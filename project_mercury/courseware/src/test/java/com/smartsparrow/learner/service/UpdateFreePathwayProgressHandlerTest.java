package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.attemptId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.deploymentId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.elementId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.progressEvent;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.progressEventCompleted;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.studentId;
import static com.smartsparrow.learner.service.UpdateProgressHandlerDataStub.mockProgressHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.FreePathwayProgress;
import com.smartsparrow.learner.progress.Progress;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Disabled
class UpdateFreePathwayProgressHandlerTest {

    @InjectMocks
    private UpdateFreePathwayProgressHandler handler;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private ProgressService progressService;

    @Mock
    private AttemptService attemptService;

    private UpdateCoursewareElementProgressEvent event;
    private CoursewareElement parentElement;
    private UpdateFreePathwayProgressHandler spy;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID pathwayId = UUID.randomUUID();

        parentElement = new CoursewareElement()
                .setElementId(pathwayId)
                .setElementType(CoursewareElementType.PATHWAY);

        event = progressEventCompleted(parentElement, CoursewareElementType.PATHWAY);

        spy = mockProgressHandler(handler);

        when(progressService.findLatestFreePathway(deploymentId, elementId, studentId)).thenReturn(Mono.empty());
        when(learnerPathwayService.findWalkables(elementId, deploymentId)).thenReturn(Flux.empty());
        when(progressService.persist(any(FreePathwayProgress.class))).thenReturn(Flux.just(new Void[]{}));
        when(attemptService.findById(eq(attemptId))).thenReturn(Mono.just(new Attempt().setParentId(UUID.randomUUID())));
    }

    @Test
    void updateProgress() {

        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                                                                                 elementId, progressActionContext);

        spy.updateProgress(exchange);

        ArgumentCaptor<Progress> broadcastCaptor = ArgumentCaptor.forClass(Progress.class);
        verify(spy, times(1))
                .broadcastProgressEventMessage(broadcastCaptor.capture(), eq(event.getUpdateProgressEvent()));

        Progress broadcastCapturedProgress = broadcastCaptor.getValue();
        assertEquals(deploymentId, broadcastCapturedProgress.getDeploymentId());
        assertEquals(elementId, broadcastCapturedProgress.getCoursewareElementId());
        assertEquals(studentId, broadcastCapturedProgress.getStudentId());

        ArgumentCaptor<Progress> propagatedCaptor = ArgumentCaptor.forClass(Progress.class);
        verify(spy, times(1)).propagateProgressChangeUpwards(
                any(Exchange.class),
                eq(event),
                propagatedCaptor.capture());
        assertEquals(broadcastCapturedProgress, propagatedCaptor.getValue());

        ArgumentCaptor<FreePathwayProgress> persistCaptor = ArgumentCaptor.forClass(FreePathwayProgress.class);
        verify(progressService, times(1)).persist(persistCaptor.capture());
        FreePathwayProgress captured = persistCaptor.getValue();
        assertNotNull(captured);
        assertEquals(captured, broadcastCapturedProgress);
        assertEquals(Float.valueOf(1.0f), captured.getCompletion().getValue());
        assertEquals(Float.valueOf(1.0f), captured.getCompletion().getConfidence());
    }

    @Test
    void updateProgress_interactiveCompleteAndPathwayComplete() {
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
        event = progressEvent(null, CoursewareElementType.PATHWAY, ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE, true);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                                                                                 elementId, progressActionContext);

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(event.getUpdateProgressEvent().getAttempt()));

        ArgumentCaptor<FreePathwayProgress> linearPathwayProgressCaptor = ArgumentCaptor.forClass(FreePathwayProgress.class);

        spy.updateProgress(exchange);

        verify(progressService).persist(linearPathwayProgressCaptor.capture());

        FreePathwayProgress persistedProgress = linearPathwayProgressCaptor.getValue();

        assertNotNull(persistedProgress);
        assertEquals(Float.valueOf(1f), persistedProgress.getCompletion().getValue());
        assertEquals(Float.valueOf(1f), persistedProgress.getCompletion().getConfidence());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(event.getElement().getElementId(), persistedProgress.getCoursewareElementId());
        assertEquals(event.getUpdateProgressEvent().getStudentId(), persistedProgress.getStudentId());
        assertEquals(event.getUpdateProgressEvent().getAttempt().getParentId(), persistedProgress.getAttemptId());
        assertNotNull(persistedProgress.getId());
        assertEquals(event.getUpdateProgressEvent().getDeploymentId(), persistedProgress.getDeploymentId());
        assertEquals(event.getUpdateProgressEvent().getChangeId(), persistedProgress.getChangeId());
    }

}
