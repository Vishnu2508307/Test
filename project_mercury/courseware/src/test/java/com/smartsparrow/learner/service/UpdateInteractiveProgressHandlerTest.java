package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.learnerPathwayId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.progressEvent;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.studentId;
import static com.smartsparrow.learner.service.UpdateProgressHandlerDataStub.mockProgressHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.Progress;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Disabled
class UpdateInteractiveProgressHandlerTest {

    @InjectMocks
    private UpdateInteractiveProgressHandler handler;

    @Mock
    private ProgressService progressService;

    @Mock
    private CoursewareHistoryService coursewareHistoryService;

    private UpdateInteractiveProgressHandler spy;

    private UpdateCoursewareElementProgressEvent event;
    private CoursewareElement parentElement;

    final ProgressActionContext progressActionContext = new ProgressActionContext()
            .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE)
            .setElementId(UUID.randomUUID())
            .setElementType(CoursewareElementType.INTERACTIVE);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        spy = mockProgressHandler(handler);
        parentElement = new CoursewareElement()
                .setElementType(CoursewareElementType.PATHWAY)
                .setElementId(learnerPathwayId);

        when(progressService.persist(any(Progress.class))).thenReturn(Flux.just(new Void[]{}));

        when(coursewareHistoryService.record(any(UUID.class), any(EvaluationResult.class), any(CoursewareElementType.class)))
                .thenReturn(Mono.just(new CompletedWalkable()));
    }

    @Test
    void updateProgress_interactiveComplete() {
        event = progressEvent(parentElement, CoursewareElementType.INTERACTIVE, ProgressionType.INTERACTIVE_COMPLETE, true);
        Exchange exchangeIn = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, progressActionContext);

        spy.updateProgress(exchangeIn);

        ArgumentCaptor<Progress> persistCaptor = ArgumentCaptor.forClass(Progress.class);
        ArgumentCaptor<Progress> broadcastCaptor = ArgumentCaptor.forClass(Progress.class);
        ArgumentCaptor<Progress> propagateCaptor = ArgumentCaptor.forClass(Progress.class);

        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getValue());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getConfidence());

        verify(spy, times(1)).propagateProgressChangeUpwards(
                any(Exchange.class),
                any(UpdateCoursewareElementProgressEvent.class),
                propagateCaptor.capture());
        assertEquals(persistCaptor.getValue(), propagateCaptor.getValue());

        verify(spy, times(1)).broadcastProgressEventMessage(
                broadcastCaptor.capture(),
                eq(event.getUpdateProgressEvent()));
        assertEquals(persistCaptor.getValue(), broadcastCaptor.getValue());

        verify(coursewareHistoryService).record(eq(studentId), any(EvaluationResult.class), eq(CoursewareElementType.INTERACTIVE));

    }

    @Test
    void updateProgress_interactiveIncomplete() {
        progressActionContext.setProgressionType(ProgressionType.INTERACTIVE_REPEAT);
        event = progressEvent(parentElement, CoursewareElementType.INTERACTIVE, ProgressionType.INTERACTIVE_REPEAT, false);
        Exchange exchangeIn = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, progressActionContext);

        spy.updateProgress(exchangeIn);

        ArgumentCaptor<Progress> persistCaptor = ArgumentCaptor.forClass(Progress.class);
        ArgumentCaptor<Progress> broadcastCaptor = ArgumentCaptor.forClass(Progress.class);
        ArgumentCaptor<Progress> propagateCaptor = ArgumentCaptor.forClass(Progress.class);

        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(0f), persistCaptor.getValue().getCompletion().getValue());
        assertEquals(Float.valueOf(0.19999999f), persistCaptor.getValue().getCompletion().getConfidence());

        verify(spy, times(1)).propagateProgressChangeUpwards(
                any(Exchange.class),
                any(UpdateCoursewareElementProgressEvent.class),
                propagateCaptor.capture());
        assertEquals(persistCaptor.getValue(), propagateCaptor.getValue());

        verify(spy, times(1)).broadcastProgressEventMessage(
                broadcastCaptor.capture(),
                eq(event.getUpdateProgressEvent()));
        assertEquals(persistCaptor.getValue(), broadcastCaptor.getValue());

        verify(coursewareHistoryService, never()).record(any(UUID.class), any(EvaluationResult.class), any(CoursewareElementType.class));
    }

    @Test
    void updateProgress_completeInteractiveAndGoTo() {
        event = progressEvent(parentElement, CoursewareElementType.INTERACTIVE, ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO, true);
        Exchange exchangeIn = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, progressActionContext);

        spy.updateProgress(exchangeIn);

        ArgumentCaptor<Progress> persistCaptor = ArgumentCaptor.forClass(Progress.class);
        ArgumentCaptor<Progress> broadcastCaptor = ArgumentCaptor.forClass(Progress.class);
        ArgumentCaptor<Progress> propagateCaptor = ArgumentCaptor.forClass(Progress.class);

        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getValue());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getConfidence());

        verify(spy, times(1)).propagateProgressChangeUpwards(
                any(Exchange.class),
                any(UpdateCoursewareElementProgressEvent.class),
                propagateCaptor.capture());
        assertEquals(persistCaptor.getValue(), propagateCaptor.getValue());

        verify(spy, times(1)).broadcastProgressEventMessage(
                broadcastCaptor.capture(),
                eq(event.getUpdateProgressEvent()));
        assertEquals(persistCaptor.getValue(), broadcastCaptor.getValue());

        verify(coursewareHistoryService).record(eq(studentId), any(EvaluationResult.class), eq(CoursewareElementType.INTERACTIVE));
    }
}
