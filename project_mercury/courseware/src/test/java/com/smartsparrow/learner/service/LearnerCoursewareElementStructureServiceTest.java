package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.courseware.pathway.LearnerPathwayMock.mockLearnerPathway;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerCoursewareElement;
import com.smartsparrow.learner.data.LearnerCoursewareWalkable;
import com.smartsparrow.learner.data.LearnerInteractive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerCoursewareElementStructureServiceTest {

    @InjectMocks
    private LearnerCoursewareElementStructureService learnerCoursewareElementStructureService;
    @Mock
    private DeploymentGateway deploymentGateway;
    @Mock
    private LearnerActivityService learnerActivityService;
    @Mock
    private LearnerInteractiveService learnerInteractiveService;
    @Mock
    private LearnerPathwayService learnerPathwayService;
    @Mock
    private LearnerService learnerService;
    @Mock
    private LearnerCoursewareService learnerCoursewareService;

    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID interactiveId1 = UUID.randomUUID();
    private static final UUID interactiveId2 = UUID.randomUUID();
    private static final UUID componentId1 = UUID.randomUUID();
    private static final UUID componentId2 = UUID.randomUUID();
    private static final UUID componentId3 = UUID.randomUUID();
    private static final UUID componentId4 = UUID.randomUUID();


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(deploymentGateway.findActivityByDeployment(any())).thenReturn(Mono.just(new DeployedActivity()
                                                                                             .setActivityId(
                                                                                                     rootElementId)));
        when(learnerActivityService.findActivity(rootElementId,
                                                 deploymentId)).thenReturn(Mono.just(new LearnerActivity()
                                                                                             .setDeploymentId(
                                                                                                     deploymentId)
                                                                                             .setId(rootElementId)));
        LearnerPathway pathway = mockLearnerPathway(pathwayId);
        when(learnerActivityService.findChildPathways(rootElementId, deploymentId)).thenReturn(Flux.just(pathway));
        when(learnerActivityService.findChildComponents(rootElementId,
                                                        deploymentId)).thenReturn(Mono.just(Collections.emptyList()));

        when(learnerPathwayService.findPathways(pathwayId, deploymentId)).thenReturn(Flux.just(pathway));
        when(learnerInteractiveService.findChildrenComponent(interactiveId1,
                                                             deploymentId)).thenReturn(Mono.just(Arrays.asList(
                componentId1,
                componentId2)));
        when(learnerInteractiveService.findChildrenComponent(interactiveId2,
                                                             deploymentId)).thenReturn(Mono.just(Arrays.asList(
                componentId3,
                componentId4)));
        when(learnerInteractiveService.findInteractive(interactiveId1,
                                                       deploymentId)).thenReturn(Mono.just(new LearnerInteractive()
                                                                                                   .setId(interactiveId1)
                                                                                                   .setDeploymentId(
                                                                                                           deploymentId)));

        when(learnerInteractiveService.findInteractive(interactiveId2,
                                                       deploymentId)).thenReturn(Mono.just(new LearnerInteractive()
                                                                                                   .setId(interactiveId2)
                                                                                                   .setDeploymentId(
                                                                                                           deploymentId)));

        when(learnerService.findElementByDeployment(rootElementId,
                                                    deploymentId)).thenReturn(Mono.just(new LearnerCoursewareElement()
                                                                                                .setId(rootElementId)
                                                                                                .setDeploymentId(
                                                                                                        deploymentId)));

        WalkableChild walkableChild_one = new WalkableChild()
                .setElementId(interactiveId1)
                .setElementType(INTERACTIVE);
        WalkableChild walkableChild_two = new WalkableChild()
                .setElementId(interactiveId2)
                .setElementType(INTERACTIVE);
        when(learnerPathwayService.findWalkables(pathwayId, deploymentId)).thenReturn(Flux.just(walkableChild_one,
                                                                                                walkableChild_two));

    }

    @Test
    void getLearnerElementStructureForRootElement_LinearPathway() {
        List<LearnerCoursewareWalkable> node = learnerCoursewareElementStructureService.getLearnerCoursewareWalkable(
                deploymentId,
                null).block();

        assertNotNull(node);
        assertEquals(5, node.size());
        LearnerCoursewareWalkable learnerCoursewareWalkable_one = node.get(0);
        assertEquals(rootElementId, learnerCoursewareWalkable_one.getElementId());
        assertEquals(ACTIVITY, learnerCoursewareWalkable_one.getType());
        assertNull(learnerCoursewareWalkable_one.getParentId());
        assertEquals(rootElementId, learnerCoursewareWalkable_one.getTopParentId());

        LearnerCoursewareWalkable learnerCoursewareWalkable_two = node.get(2);

        assertEquals(INTERACTIVE, learnerCoursewareWalkable_two.getType());
        assertNotNull(learnerCoursewareWalkable_two.getParentId());
        assertEquals(rootElementId, learnerCoursewareWalkable_two.getTopParentId());
    }

    @Test
    void getLearnerElementStructureForWithoutElementId_FreePathway() {
        LearnerPathway pathway = mockLearnerPathway(pathwayId,
                                                    PathwayType.FREE,
                                                    deploymentId,
                                                    UUID.randomUUID(),
                                                    null,
                                                    PreloadPathway.ALL);
        when(learnerActivityService.findChildPathways(rootElementId, deploymentId)).thenReturn(Flux.just(pathway));
        when(learnerPathwayService.findPathways(pathwayId, deploymentId)).thenReturn(Flux.just(pathway));

        List<LearnerCoursewareWalkable> node = learnerCoursewareElementStructureService.getLearnerCoursewareWalkable(
                deploymentId,
                null).block();

        assertNotNull(node);
        assertEquals(8, node.size());
        LearnerCoursewareWalkable learnerCoursewareWalkable_one = node.get(0);
        assertEquals(rootElementId, learnerCoursewareWalkable_one.getElementId());
        assertEquals(ACTIVITY, learnerCoursewareWalkable_one.getType());
        assertNull(learnerCoursewareWalkable_one.getParentId());
        assertEquals(rootElementId, learnerCoursewareWalkable_one.getTopParentId());

        LearnerCoursewareWalkable learnerCoursewareWalkable_two = node.get(2);

        assertEquals(INTERACTIVE, learnerCoursewareWalkable_two.getType());
        assertNotNull(learnerCoursewareWalkable_two.getParentId());
        assertEquals(rootElementId, learnerCoursewareWalkable_two.getTopParentId());

        LearnerCoursewareWalkable learnerCoursewareWalkable_four = node.get(5);

        assertEquals(INTERACTIVE, learnerCoursewareWalkable_four.getType());
        assertNotNull(learnerCoursewareWalkable_four.getParentId());
        assertEquals(rootElementId, learnerCoursewareWalkable_four.getTopParentId());

    }

    @Test
    void getCoursewareElementStructureForWithElementId() {
        LearnerPathway pathway = mockLearnerPathway(pathwayId,
                                                    PathwayType.FREE,
                                                    deploymentId,
                                                    UUID.randomUUID(),
                                                    null,
                                                    PreloadPathway.ALL);
        when(learnerActivityService.findChildPathways(rootElementId, deploymentId)).thenReturn(Flux.just(pathway));
        when(learnerPathwayService.findPathways(pathwayId, deploymentId)).thenReturn(Flux.just(pathway));
        when(learnerCoursewareService.getLearnerRootElementId(deploymentId,
                                                              rootElementId,
                                                              ACTIVITY)).thenReturn(Mono.just(rootElementId));
        when(learnerService.findElementByDeployment(rootElementId,
                                                    deploymentId)).thenReturn(Mono.just(new LearnerCoursewareElement()
                                                                                                .setDeploymentId(
                                                                                                        deploymentId)
                                                                                                .setElementType(ACTIVITY)
                                                                                                .setId(rootElementId)));

        List<LearnerCoursewareWalkable> node = learnerCoursewareElementStructureService.getLearnerCoursewareWalkable(
                deploymentId,
                rootElementId).block();

        assertNotNull(node);
        assertEquals(8, node.size());
        LearnerCoursewareWalkable learnerCoursewareWalkable_one = node.get(0);
        assertEquals(rootElementId, learnerCoursewareWalkable_one.getElementId());
        assertEquals(ACTIVITY, learnerCoursewareWalkable_one.getType());
        assertNull(learnerCoursewareWalkable_one.getParentId());
        assertEquals(rootElementId, learnerCoursewareWalkable_one.getTopParentId());

        LearnerCoursewareWalkable learnerCoursewareWalkable_two = node.get(2);

        assertEquals(INTERACTIVE, learnerCoursewareWalkable_two.getType());
        assertNotNull(learnerCoursewareWalkable_two.getParentId());
        assertEquals(rootElementId, learnerCoursewareWalkable_two.getTopParentId());

        LearnerCoursewareWalkable learnerCoursewareWalkable_four = node.get(5);

        assertEquals(INTERACTIVE, learnerCoursewareWalkable_four.getType());
        assertNotNull(learnerCoursewareWalkable_four.getParentId());
        assertEquals(rootElementId, learnerCoursewareWalkable_four.getTopParentId());

    }
}
