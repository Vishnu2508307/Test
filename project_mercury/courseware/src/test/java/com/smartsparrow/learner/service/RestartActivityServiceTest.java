package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.attemptId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.changeId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.deploymentId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.studentId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.ActivityProgress;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class RestartActivityServiceTest {

    @InjectMocks
    private RestartActivityService restartActivityService;

    private UUID activityId = UUID.randomUUID();
    private UUID parentAttemptId = UUID.randomUUID();

    @Mock
    private ProgressService progressService;
    @Mock
    private DeploymentService deploymentService;
    @Mock
    private CamelReactiveStreamsService camel;
    @Mock
    private LearnerCoursewareService learnerCoursewareService;
    @Mock
    private AcquireAttemptService acquireAttemptService;
    @Mock
    private AttemptService attemptService;
    @Mock
    private StudentScopeService studentScopeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Attempt latestActivityAttempt = new Attempt().setParentId(parentAttemptId).setValue(1);

        when(acquireAttemptService.acquireLatestActivityAttempt(deploymentId, activityId, studentId)).thenReturn(Mono.just(latestActivityAttempt));
        when(attemptService.newAttempt(deploymentId, studentId, CoursewareElementType.ACTIVITY, activityId, parentAttemptId, 2))
                .thenAnswer((Answer<Mono<Attempt>>) invocation -> Mono.just(new Attempt()
                        .setId(attemptId)
                        .setDeploymentId(invocation.getArgument(0))
                        .setStudentId(invocation.getArgument(1))
                        .setCoursewareElementType(invocation.getArgument(2))
                        .setCoursewareElementId(invocation.getArgument(3))
                        .setParentId(invocation.getArgument(4))
                        .setValue(invocation.getArgument(5))));

        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity().setChangeId(changeId)));
        when(progressService.persist(any(ActivityProgress.class))).thenReturn(Flux.empty());
        when(studentScopeService.resetScopesFor(any(UUID.class), any(UUID.class), any(UUID.class))).thenReturn(Flux.empty());
    }

    @Test
    void restartActivity_noParentPathway() {
        when(learnerCoursewareService.getAncestry(deploymentId, activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(
                        new CoursewareElement(activityId, CoursewareElementType.ACTIVITY)
                )));

        ActivityProgress progress = restartActivityService.restartActivity(deploymentId, activityId, studentId).block();

        verify(progressService).persist(progress);
        assertNotNull(progress);
        assertEquals(0f, progress.getCompletion().getConfidence().floatValue());
        assertEquals(0f, progress.getCompletion().getValue().floatValue());
        assertEquals(attemptId, progress.getAttemptId());
        assertEquals(changeId, progress.getChangeId());
        assertEquals(deploymentId, progress.getDeploymentId());
        assertTrue(progress.getChildWalkableCompletionConfidences().isEmpty());
        assertTrue(progress.getChildWalkableCompletionValues().isEmpty());
        assertEquals(activityId, progress.getCoursewareElementId());
        assertEquals(CoursewareElementType.ACTIVITY, progress.getCoursewareElementType());
        assertNull(progress.getEvaluationId());
        assertNotNull(progress.getId());
    }

    @Test
    void restartActivity_propagateEventsToParent() {
        UUID parentPathwayId = UUID.randomUUID();

        when(learnerCoursewareService.getAncestry(deploymentId, activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(
                        new CoursewareElement(activityId, CoursewareElementType.ACTIVITY),
                        new CoursewareElement(parentPathwayId, CoursewareElementType.PATHWAY)
                )));

        TestPublisher<Exchange> testPublisher = TestPublisher.create();
        testPublisher.next(mock(Exchange.class));

        when(camel.to(anyString(), any(UpdateCoursewareElementProgressEvent.class))).thenReturn(testPublisher.complete());

        ActivityProgress progress = restartActivityService.restartActivity(deploymentId, activityId, studentId).block();

        ArgumentCaptor<UpdateCoursewareElementProgressEvent> eventCaptor = ArgumentCaptor.forClass(UpdateCoursewareElementProgressEvent.class);
        verify(camel).to(any(), eventCaptor.capture());
        UpdateCoursewareElementProgressEvent event = eventCaptor.getValue();
        assertEquals(parentPathwayId, event.getElement().getElementId());
        assertEquals(CoursewareElementType.PATHWAY, event.getElement().getElementType());
        assertNotNull(event.getUpdateProgressEvent());
        assertEquals(1, event.getEventProgress().size());
        assertEquals(progress, event.getEventProgress().get(0));
        assertEquals(deploymentId, event.getUpdateProgressEvent().getDeploymentId());
        assertEquals(studentId, event.getUpdateProgressEvent().getStudentId());
        assertEquals(changeId, event.getUpdateProgressEvent().getChangeId());
        assertNull(event.getUpdateProgressEvent().getEvaluationId());
        assertEquals(attemptId, event.getUpdateProgressEvent().getAttemptId());
    }
}
