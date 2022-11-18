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

import java.util.HashMap;
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
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.LinearPathwayProgress;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Disabled
class UpdateLinearPathwayProgressHandlerTest {

    @InjectMocks
    private UpdateLinearPathwayProgressHandler handler;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private ProgressService progressService;

    @Mock
    private AttemptService attemptService;

    private UpdateCoursewareElementProgressEvent event;
    private UpdateLinearPathwayProgressHandler spy;
    private CoursewareElement parentElement;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID pathwayId = UUID.randomUUID();

        parentElement = new CoursewareElement()
                .setElementId(pathwayId)
                .setElementType(CoursewareElementType.PATHWAY);

        spy = mockProgressHandler(handler);

        when(progressService.findLatestLinearPathway(deploymentId, elementId, studentId)).thenReturn(Mono.empty());
        when(learnerPathwayService.findWalkables(elementId, deploymentId)).thenReturn(Flux.empty());
        when(progressService.persist(any(LinearPathwayProgress.class))).thenReturn(Flux.just(new Void[]{}));
        when(attemptService.findById(eq(attemptId))).thenReturn(Mono.just(new Attempt().setParentId(UUID.randomUUID())));
    }

    @Test
    void updateProgress() {

        updatePathwayProgressCommonTest();
    }

    @Test
    void updateProgress_withChildren() {
        UUID interactiveId = UUID.randomUUID();
        WalkableChild child = new WalkableChild()
                .setElementId(interactiveId)
                .setElementType(CoursewareElementType.INTERACTIVE);
        LinearPathwayProgress progress = new LinearPathwayProgress()
                .setChildWalkableCompletionValues(new HashMap<UUID, Float>() {
                    {
                        put(interactiveId, 0.5f);
                    }
                })
                .setChildWalkableCompletionConfidences(new HashMap<UUID, Float>() {
                    {
                        put(interactiveId, 0.5f);
                    }
                });
        when(learnerPathwayService.findWalkables(elementId, deploymentId)).thenReturn(Flux.just(child));
        when(progressService.findLatestLinearPathway(deploymentId, elementId, studentId)).thenReturn(Mono.just(progress));

        updatePathwayProgressCommonTest();

    }

    @Test
    void updateProgress_interactiveCompleteAndPathwayComplete() {
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
        event = progressEvent(null, CoursewareElementType.PATHWAY, ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE, true);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                elementId,
                progressActionContext);

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(event.getUpdateProgressEvent().getAttempt()));

        ArgumentCaptor<LinearPathwayProgress> linearPathwayProgressCaptor = ArgumentCaptor.forClass(LinearPathwayProgress.class);

        spy.updateProgress(exchange);

        verify(progressService).persist(linearPathwayProgressCaptor.capture());

        LinearPathwayProgress persistedProgress = linearPathwayProgressCaptor.getValue();

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

    private void updatePathwayProgressCommonTest() {
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
        event = progressEventCompleted(parentElement, CoursewareElementType.PATHWAY);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                parentElement.getElementId(),
                progressActionContext);

        spy.updateProgress(exchange);

        ArgumentCaptor<LinearPathwayProgress> persistCaptor = ArgumentCaptor.forClass(LinearPathwayProgress.class);
        ArgumentCaptor<LinearPathwayProgress> broadcastCaptor = ArgumentCaptor.forClass(LinearPathwayProgress.class);
        ArgumentCaptor<LinearPathwayProgress> propagateCaptor = ArgumentCaptor.forClass(LinearPathwayProgress.class);

        verify(progressService).persist(persistCaptor.capture());

        verify(spy, times(1)).propagateProgressChangeUpwards(
                eq(exchange),
                eq(event),
                propagateCaptor.capture());

        verify(spy, times(1)).broadcastProgressEventMessage(
                broadcastCaptor.capture(),
                eq(event.getUpdateProgressEvent()));

        LinearPathwayProgress capturedPersist = persistCaptor.getValue();
        LinearPathwayProgress broadcastPersist = broadcastCaptor.getValue();
        LinearPathwayProgress propagatePersist = propagateCaptor.getValue();
        assertEquals(capturedPersist, broadcastPersist);
        assertEquals(capturedPersist, propagatePersist);

        assertEquals(deploymentId, capturedPersist.getDeploymentId());
        assertEquals(elementId, capturedPersist.getCoursewareElementId());
        assertEquals(studentId, capturedPersist.getStudentId());

        assertNotNull(capturedPersist);
        assertEquals(Float.valueOf(1.0f), capturedPersist.getCompletion().getValue());
        assertEquals(Float.valueOf(1.0f), capturedPersist.getCompletion().getConfidence());
    }

}
