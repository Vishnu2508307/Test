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
import com.smartsparrow.learner.progress.RandomPathwayProgress;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerRandomPathwayTest {

    private LearnerRandomPathway learnerRandomPathway;

    @Mock
    private ProgressService progressService;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private RandomPathwayProgress randomPathwayProgress;

    private static final UUID id = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();

    private static WalkableChild walkableOne = buildWalkableChild();
    private static WalkableChild walkableTwo = buildWalkableChild();
    private static WalkableChild walkableThree = buildWalkableChild();
    private static WalkableChild walkableFour = buildWalkableChild();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        learnerRandomPathway = new LearnerRandomPathway(progressService, learnerPathwayService);

        learnerRandomPathway
                .setId(id)
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
        .setConfig("{\"exitAfter\": 2}");

        when(randomPathwayProgress.getId()).thenReturn(UUID.randomUUID());
        when(progressService.findLatestRandomPathway(deploymentId, id, studentId)).thenReturn(Mono.just(randomPathwayProgress));
        when(learnerPathwayService.findWalkables(id, deploymentId)).thenReturn(Flux.just(
                walkableOne,
                walkableTwo,
                walkableThree,
                walkableFour
        ));
    }

    @Test
    void supplyRelevantWalkables_noAttempt() {
        when(progressService.findLatestRandomPathway(deploymentId, id, studentId)).thenReturn(Mono.empty());

        List<WalkableChild> relevantWalkables = learnerRandomPathway.supplyRelevantWalkables(studentId)
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
        when(randomPathwayProgress.getInProgressElementId()).thenReturn(inProgressId);
        when(randomPathwayProgress.getInProgressElementType()).thenReturn(CoursewareElementType.ACTIVITY);

        List<WalkableChild> relevantWalkables = learnerRandomPathway.supplyRelevantWalkables(studentId)
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
        when(randomPathwayProgress.getCompletedWalkables()).thenReturn(Lists.newArrayList(walkableOne.getElementId()));
        when(randomPathwayProgress.getInProgressElementId()).thenReturn(null);
        List<WalkableChild> relevantWalkables = learnerRandomPathway.supplyRelevantWalkables(studentId)
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
        when(randomPathwayProgress.getCompletedWalkables())
                .thenReturn(Lists.newArrayList(walkableOne.getElementId(), walkableFour.getElementId()));
        when(randomPathwayProgress.getInProgressElementId()).thenReturn(null);
        List<WalkableChild> relevantWalkables = learnerRandomPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(0, relevantWalkables.size());
    }

}