package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.AttemptStubs.activityAttemptId;
import static com.smartsparrow.learner.service.AttemptStubs.activityId;
import static com.smartsparrow.learner.service.AttemptStubs.deploymentId;
import static com.smartsparrow.learner.service.AttemptStubs.interactiveAttemptId;
import static com.smartsparrow.learner.service.AttemptStubs.interactiveId;
import static com.smartsparrow.learner.service.AttemptStubs.mockAttemptService;
import static com.smartsparrow.learner.service.AttemptStubs.pathwayAttemptId;
import static com.smartsparrow.learner.service.AttemptStubs.pathwayId;
import static com.smartsparrow.learner.service.AttemptStubs.studentId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.learner.lang.LearnerPathwayNotFoundFault;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AcquireAttemptServiceTest {

    @InjectMocks
    private AcquireAttemptService acquireAttemptService;

    @Mock
    private AttemptService attemptService;

    @Mock
    private LearnerActivityService learnerActivityService;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private LearnerInteractiveService learnerInteractiveService;

    @Mock
    private PathwayAttemptResolverProvider pathwayAttemptResolverProvider;

    @Mock
    private PathwayAttemptResolver pathwayAttemptResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        // The setup for this test class creates the following structure
        //
        //        ------------
        //       | activityId |
        //        ------------
        //              |
        //              |
        //        -------------
        //       |  pathwayId  |
        //        -------------
        //              |
        //              |
        //       ---------------
        //      | interactiveId |
        //       ---------------

        when(learnerInteractiveService.findParentPathwayId(interactiveId, deploymentId)).thenReturn(Mono.just(pathwayId));
        when(learnerPathwayService.findParentActivityId(pathwayId, deploymentId)).thenReturn(Mono.just(activityId));
        TestPublisher<UUID> parentPathwayPublisher = TestPublisher.create();
        parentPathwayPublisher.error(new LearnerPathwayNotFoundFault("parent pathway not found for activity" + activityId));
        when(learnerActivityService.findParentPathwayId(activityId, deploymentId)).thenReturn(parentPathwayPublisher.mono());

        mockAttemptService(attemptService);

        TestPublisher<Attempt> latestAttemptPublisher = TestPublisher.<Attempt>create().error(new AttemptNotFoundFault("attempt not found"));
        when(attemptService.findLatestAttempt(eq(deploymentId), any(UUID.class), eq(studentId))).thenReturn(latestAttemptPublisher.mono());
        LearnerPathway pathwayMock = mock(LearnerPathway.class);
        when(pathwayMock.getType()).thenReturn(PathwayType.LINEAR);
        when(learnerPathwayService.find(pathwayId, deploymentId)).thenReturn(Mono.just(pathwayMock));
        when(pathwayAttemptResolverProvider.get(PathwayType.LINEAR)).thenReturn(pathwayAttemptResolver);
        when(pathwayAttemptResolver.resolveInteractiveAttempt(eq(deploymentId), eq(interactiveId), eq(studentId),
                any(Attempt.class), any(Attempt.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(4)));
    }

    @Test
    @DisplayName("It should return the latest attempt for a root activity element")
    void acquireLatestActivityAttempt_noParentPathway() {
        Attempt attempt = acquireAttemptService.acquireLatestActivityAttempt(deploymentId, activityId, studentId).block();

        assertNotNull(attempt);
        assertEquals(activityId, attempt.getCoursewareElementId());
        assertEquals(CoursewareElementType.ACTIVITY, attempt.getCoursewareElementType());
        assertEquals(deploymentId, attempt.getDeploymentId());
        assertEquals(studentId, attempt.getStudentId());
        assertEquals(Integer.valueOf(1), attempt.getValue());
        assertNotNull(attempt.getId());
        assertNull(attempt.getParentId());
    }

    @Test
    @DisplayName("It should return the a new activity attempt when the parent attempt has changed")
    void acquireLatestActivityAttempt_withParentPathway_differentAttempt() {

        // In this test case we are using the following structure
        //
        //        ------------------
        //       | parentActivityId |
        //        ------------------
        //              |
        //              |
        //        -------------------
        //       |  parentPathwayId  |
        //        -------------------
        //              |
        //              |
        //       ---------------
        //      | activityId    |
        //       ---------------

        UUID parentPathwayId = UUID.randomUUID();
        UUID parentActivityId = UUID.randomUUID();
        UUID parentPathwayAttemptId = UUID.randomUUID();
        UUID parentActivityAttemptId = UUID.randomUUID();

        Attempt newParentActivityAttempt = new Attempt().setId(parentActivityAttemptId).setParentId(null);
        Attempt newParentPathwayAttempt = new Attempt().setId(parentPathwayAttemptId).setParentId(parentActivityAttemptId);

        Attempt oldActivityAttempt = new Attempt()
                .setId(UUID.randomUUID())
                .setCoursewareElementType(CoursewareElementType.ACTIVITY)
                .setParentId(UUID.randomUUID());

        TestPublisher<UUID> parentPathwayPublisher = TestPublisher.create();
        parentPathwayPublisher.error(new LearnerPathwayNotFoundFault("parent pathway not found for activity" + parentActivityId));
        when(learnerActivityService.findParentPathwayId(parentActivityId, deploymentId)).thenReturn(parentPathwayPublisher.mono());
        when(learnerPathwayService.findParentActivityId(parentPathwayId, deploymentId)).thenReturn(Mono.just(parentActivityId));
        when(learnerActivityService.findParentPathwayId(activityId, deploymentId)).thenReturn(Mono.just(parentPathwayId));

        when(attemptService.findLatestAttempt(deploymentId, parentActivityId, studentId)).thenReturn(Mono.just(newParentActivityAttempt));
        when(attemptService.findLatestAttempt(deploymentId, parentPathwayId, studentId)).thenReturn(Mono.just(newParentPathwayAttempt));
        when(attemptService.findLatestAttempt(deploymentId, activityId, studentId)).thenReturn(Mono.just(oldActivityAttempt));

        Attempt result = acquireAttemptService.acquireLatestActivityAttempt(deploymentId, activityId, studentId).block();

        assertNotNull(result);
        assertEquals(activityId, result.getCoursewareElementId());
        assertEquals(CoursewareElementType.ACTIVITY, result.getCoursewareElementType());
        assertEquals(deploymentId, result.getDeploymentId());
        assertEquals(studentId, result.getStudentId());
        assertEquals(Integer.valueOf(1), result.getValue());
        assertEquals(activityAttemptId, result.getId());
        assertEquals(parentPathwayAttemptId, result.getParentId());

        verify(attemptService)
                .newAttempt(eq(deploymentId), eq(studentId), eq(CoursewareElementType.ACTIVITY), eq(activityId), eq(parentPathwayAttemptId));
    }

    @Test
    @DisplayName("It should throw an IllegalStateException if the parent activity is not found for the pathway")
    void acquireLatestPathwayAttempt_noParentActivity() {
        TestPublisher<UUID> parentActivityPublisher = TestPublisher.create();
        parentActivityPublisher.error(new ParentActivityNotFoundException(pathwayId));
        when(learnerPathwayService.findParentActivityId(pathwayId, deploymentId)).thenReturn(parentActivityPublisher.mono());

        IllegalStateException ise = assertThrows(IllegalStateException.class,
                () -> acquireAttemptService.acquireLatestPathwayAttempt(deploymentId, pathwayId, studentId).block());

        assertEquals("Unable to find attempt", ise.getMessage());

    }

    @Test
    @DisplayName("It should return the latest pathway attempt with the parent id set")
    void acquireLatestPathwayAttempt_withParentActivity() {

        Attempt attempt = acquireAttemptService.acquireLatestPathwayAttempt(deploymentId, pathwayId, studentId).block();

        assertNotNull(attempt);
        assertEquals(pathwayId, attempt.getCoursewareElementId());
        assertEquals(CoursewareElementType.PATHWAY, attempt.getCoursewareElementType());
        assertEquals(deploymentId, attempt.getDeploymentId());
        assertEquals(studentId, attempt.getStudentId());
        assertEquals(Integer.valueOf(1), attempt.getValue());
        assertEquals(pathwayAttemptId, attempt.getId());
        assertEquals(activityAttemptId, attempt.getParentId());

        verify(attemptService)
                .newAttempt(eq(deploymentId), eq(studentId), eq(CoursewareElementType.ACTIVITY), eq(activityId), eq(null));
        verify(attemptService)
                .newAttempt(eq(deploymentId), eq(studentId), eq(CoursewareElementType.PATHWAY), eq(pathwayId), eq(activityAttemptId));
    }

    @Test
    @DisplayName("It should throw an IllegalStateException when the parent pathway is not found for the interactive")
    void acquireLatestInteractiveAttempt_fails_noParentPathway() {

        TestPublisher<UUID> publisher = TestPublisher.create();
        publisher.error(new ParentPathwayNotFoundException(interactiveId));

        when(learnerInteractiveService.findParentPathwayId(interactiveId, deploymentId)).thenReturn(publisher.mono());

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> acquireAttemptService.acquireLatestInteractiveAttempt(deploymentId, interactiveId, studentId).block());

        assertEquals("Unable to find attempt", e.getMessage());
    }

    @Test
    @DisplayName("It should throw an IllegalStateException when the parent activity for the parentPathway is not found")
    void acquireLatestInteractiveAttempt_fails_noParentActivityForParentPathway() {
        TestPublisher<UUID> parentActivityPublisher = TestPublisher.create();
        parentActivityPublisher.error(new ParentActivityNotFoundException(pathwayId));
        when(learnerPathwayService.findParentActivityId(pathwayId, deploymentId)).thenReturn(parentActivityPublisher.mono());

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> acquireAttemptService.acquireLatestInteractiveAttempt(deploymentId, interactiveId, studentId).block());

        assertEquals("Unable to find attempt", e.getMessage());
    }

    @Test
    void acquireLatestInteractiveAttempt_success() {
        Attempt latestInteractiveAttempt = acquireAttemptService
                .acquireLatestInteractiveAttempt(deploymentId, interactiveId, studentId).block();

        assertNotNull(latestInteractiveAttempt);
        assertEquals(interactiveAttemptId, latestInteractiveAttempt.getId());
        assertEquals(interactiveId, latestInteractiveAttempt.getCoursewareElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, latestInteractiveAttempt.getCoursewareElementType());
        assertEquals(deploymentId, latestInteractiveAttempt.getDeploymentId());
        assertEquals(studentId, latestInteractiveAttempt.getStudentId());
        assertEquals(Integer.valueOf(1), latestInteractiveAttempt.getValue());
        assertEquals(pathwayAttemptId, latestInteractiveAttempt.getParentId());

        verify(attemptService)
                .newAttempt(eq(deploymentId), eq(studentId), eq(CoursewareElementType.ACTIVITY), eq(activityId), eq(null));
        verify(attemptService)
                .newAttempt(eq(deploymentId), eq(studentId), eq(CoursewareElementType.PATHWAY), eq(pathwayId), eq(activityAttemptId));
        verify(attemptService)
                .newAttempt(eq(deploymentId), eq(studentId), eq(CoursewareElementType.INTERACTIVE), eq(interactiveId), eq(pathwayAttemptId));
    }

}
