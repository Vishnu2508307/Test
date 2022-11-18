package com.smartsparrow.courseware.pathway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.learner.progress.GraphPathwayProgress;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerGraphPathwayTest {

    private LearnerGraphPathway learnerGraphPathway;
    @Mock
    private ProgressService progressService;
    @Mock
    private LearnerPathwayService learnerPathwayService;

    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final CoursewareElement startingWalkable = new CoursewareElement()
            .setElementId(UUID.randomUUID())
            .setElementType(CoursewareElementType.INTERACTIVE);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        learnerGraphPathway = new LearnerGraphPathway(
                progressService,
                learnerPathwayService
        );

        learnerGraphPathway.setId(pathwayId)
                .setChangeId(changeId)
                .setDeploymentId(deploymentId)
                .setConfig("{" +
                            "\"startingWalkableId\":\"" + startingWalkable.getElementId().toString() + "\"," +
                            "\"startingWalkableType\":\"" + startingWalkable.getElementType().toString() + "\"" +
                            "}");
    }

    @Test
    void supplyRelevantWalkables_startingWalkable() {
        when(progressService.findLatestGraphPathway(deploymentId, pathwayId, studentId))
                .thenReturn(Mono.empty());

        List<WalkableChild> relevantWalkables = learnerGraphPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(1, relevantWalkables.size());

        WalkableChild relevantWalkable = relevantWalkables.get(0);

        assertNotNull(relevantWalkable);
        assertEquals(startingWalkable.getElementId(), relevantWalkable.getElementId());
        assertEquals(startingWalkable.getElementType(), relevantWalkable.getElementType());
    }

    @SuppressWarnings("unchecked")
    @Test
    void supplyRelevantWalkables_startingWalkable_noFields() {
        final UUID interactiveId = UUID.randomUUID();
        learnerGraphPathway.setConfig("{}");
        when(progressService.findLatestGraphPathway(deploymentId, pathwayId, studentId))
                .thenReturn(Mono.empty());

        when(learnerPathwayService.findWalkables(learnerGraphPathway.getId(), learnerGraphPathway.getDeploymentId()))
                .thenReturn(Flux.just(new WalkableChild()
                                .setElementId(interactiveId)
                                .setElementType(CoursewareElementType.INTERACTIVE),
                        new WalkableChild()
                                .setElementId(UUID.randomUUID())
                                .setElementType(CoursewareElementType.ACTIVITY)));

        List<WalkableChild> relevantWalkables = learnerGraphPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(1, relevantWalkables.size());

        WalkableChild relevantWalkable = relevantWalkables.get(0);

        assertNotNull(relevantWalkable);
        assertEquals(interactiveId, relevantWalkable.getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, relevantWalkable.getElementType());
    }

    @SuppressWarnings("unchecked")
    @Test
    void supplyRelevantWalkables_startingWalkable_invalidFields() {
        final UUID interactiveId = UUID.randomUUID();
        learnerGraphPathway.setConfig("{" +
                "\"startingWalkableId\":\"\"," +
                "\"startingWalkableType\":\"null\"" +
                "}");
        when(progressService.findLatestGraphPathway(deploymentId, pathwayId, studentId))
                .thenReturn(Mono.empty());

        when(learnerPathwayService.findWalkables(learnerGraphPathway.getId(), learnerGraphPathway.getDeploymentId()))
                .thenReturn(Flux.just(new WalkableChild()
                                .setElementId(interactiveId)
                                .setElementType(CoursewareElementType.INTERACTIVE),
                        new WalkableChild()
                                .setElementId(UUID.randomUUID())
                                .setElementType(CoursewareElementType.ACTIVITY)));

        List<WalkableChild> relevantWalkables = learnerGraphPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(1, relevantWalkables.size());

        WalkableChild relevantWalkable = relevantWalkables.get(0);

        assertNotNull(relevantWalkable);
        assertEquals(interactiveId, relevantWalkable.getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, relevantWalkable.getElementType());
    }

    @SuppressWarnings("unchecked")
    @Test
    void supplyRelevantWalkables_currentWalkable_fromProgress() {
        GraphPathwayProgress progress = new GraphPathwayProgress()
                .setCurrentWalkableId(UUID.randomUUID())
                .setCurrentWalkableType(CoursewareElementType.ACTIVITY);

        when(progressService.findLatestGraphPathway(deploymentId, pathwayId, studentId))
                .thenReturn(Mono.just(progress));

        List<WalkableChild> relevantWalkables = learnerGraphPathway.supplyRelevantWalkables(studentId)
                .collectList()
                .block();

        assertNotNull(relevantWalkables);
        assertEquals(1, relevantWalkables.size());

        WalkableChild relevantWalkable = relevantWalkables.get(0);

        assertNotNull(relevantWalkable);
        assertEquals(progress.getCurrentWalkableId(), relevantWalkable.getElementId());
        assertEquals(progress.getCurrentWalkableType(), relevantWalkable.getElementType());
    }

}
