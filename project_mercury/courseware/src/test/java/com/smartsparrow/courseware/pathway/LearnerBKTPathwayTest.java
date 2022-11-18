package com.smartsparrow.courseware.pathway;

import static com.smartsparrow.courseware.pathway.WalkableChildStub.buildWalkableChild;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerBKTPathwayTest {

    private LearnerBKTPathway learnerBKTPathway;

    @Mock
    private ProgressService progressService;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private BKTPathwayProgress bktPathwayProgress;

    private static final UUID id = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID documentId = UUID.randomUUID();
    private static final UUID documentItemId = UUID.randomUUID();

    private static WalkableChild walkableOne = buildWalkableChild();
    private static WalkableChild walkableTwo = buildWalkableChild();
    private static WalkableChild walkableThree = buildWalkableChild();
    private static WalkableChild walkableFour = buildWalkableChild();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        learnerBKTPathway = new LearnerBKTPathway(progressService, learnerPathwayService);

        learnerBKTPathway
                .setId(id)
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
                .setConfig("{\"exitAfter\": 2," +
                           "\"P_G\": 0.2," +
                           "\"P_S\": 0.2," +
                           "\"P_T\": 0.9," +
                           "\"P_L0\":0.4," +
                           "\"P_LN\":0.9," +
                           "\"competency\": [{" +
                                "\"documentId\": \"" + documentId + "\"," +
                                "\"documentItemId\": \"" + documentItemId + "\"" +
                            "}]," +
                           "\"maintainFor\": 3 }");

        when(bktPathwayProgress.getId()).thenReturn(UUID.randomUUID());
        when(progressService.findLatestNBKTPathway(deploymentId, id, studentId, 1)).thenReturn(Flux.just(bktPathwayProgress));
        when(learnerPathwayService.findWalkables(id, deploymentId)).thenReturn(Flux.just(
                walkableOne,
                walkableTwo,
                walkableThree,
                walkableFour
        ));
    }

    @Test
    void getExitAfter() {
        assertEquals(Integer.valueOf(2), learnerBKTPathway.getExitAfter());
    }

    @Test
    void getGuessProbability() {
        assertEquals(Double.valueOf(0.2), learnerBKTPathway.getGuessProbability());
    }

    @Test
    void getSlipProbability() {
        assertEquals(Double.valueOf(0.2), learnerBKTPathway.getSlipProbability());
    }

    @Test
    void getTransitProbability() {
        assertEquals(Double.valueOf(0.9), learnerBKTPathway.getTransitProbability());
    }

    @Test
    void getL0() {
        assertEquals(Double.valueOf(0.4), learnerBKTPathway.getL0());
    }

    @Test
    void getPLN() {
        assertEquals(Double.valueOf(0.9), learnerBKTPathway.getPLN());
    }

    @Test
    void getMaintainFor() {
        assertEquals(Integer.valueOf(3), learnerBKTPathway.getMaintainFor());
    }

    @Test
    void getCompetency() {
        List<BKTPathway.ConfiguredDocumentItem> configuredItems = learnerBKTPathway.getCompetency();

        assertNotNull(configuredItems);
        assertEquals(1, configuredItems.size());

        BKTPathway.ConfiguredDocumentItem configuredDocumentItem = configuredItems.get(0);

        assertNotNull(configuredDocumentItem);
        assertEquals(documentId, configuredDocumentItem.getDocumentId());
        assertEquals(documentItemId, configuredDocumentItem.getDocumentItemId());
    }

    @Test
    void getCompetency_empty() {
        learnerBKTPathway
                .setConfig("{\"exitAfter\": 2," +
                        "\"P_G\": 0.2," +
                        "\"P_S\": 0.2," +
                        "\"P_T\": 0.9," +
                        "\"P_L0\":0.4," +
                        "\"P_LN\":0.9," +
                        "\"competency\": []," +
                        "\"maintainFor\": 3 }");

        List<BKTPathway.ConfiguredDocumentItem> configuredItems = learnerBKTPathway.getCompetency();

        assertNotNull(configuredItems);
        assertEquals(0, configuredItems.size());
    }

    @Test
    void supplyRelevantWalkables_noAttempt() {
        when(progressService.findLatestRandomPathway(deploymentId, id, studentId)).thenReturn(Mono.empty());

        List<WalkableChild> relevantWalkables = learnerBKTPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(1, relevantWalkables.size());

        final WalkableChild child = relevantWalkables.get(0);

        assertNotNull(child);

        assertTrue(Lists.newArrayList(walkableOne, walkableTwo, walkableThree, walkableFour).stream()
                .anyMatch(one -> one.equals(child)));
    }

    @Test
    void supplyRelevantWalkables_inProgress() {
        UUID inProgressId = UUID.randomUUID();
        when(bktPathwayProgress.getInProgressElementId()).thenReturn(inProgressId);
        when(bktPathwayProgress.getInProgressElementType()).thenReturn(CoursewareElementType.ACTIVITY);

        List<WalkableChild> relevantWalkables = learnerBKTPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(1, relevantWalkables.size());

        final WalkableChild child = relevantWalkables.get(0);

        assertNotNull(child);

        assertEquals(inProgressId, child.getElementId());
    }

    @Test
    void supplyRelevantWalkables_noInProgress() {
        when(bktPathwayProgress.getCompletedWalkables()).thenReturn(Lists.newArrayList(walkableOne.getElementId()));
        when(bktPathwayProgress.getInProgressElementId()).thenReturn(null);
        List<WalkableChild> relevantWalkables = learnerBKTPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(1, relevantWalkables.size());

        final WalkableChild child = relevantWalkables.get(0);

        assertNotNull(child);

        assertTrue(Lists.newArrayList(walkableTwo, walkableThree, walkableFour).stream()
                .anyMatch(one -> one.equals(child)));

        assertNotEquals(child, walkableOne);
    }

    @Test
    void supplyRelevantWalkables_completed() {
        when(bktPathwayProgress.isCompleted()).thenReturn(true);
        List<WalkableChild> relevantWalkables = learnerBKTPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(0, relevantWalkables.size());
    }
}