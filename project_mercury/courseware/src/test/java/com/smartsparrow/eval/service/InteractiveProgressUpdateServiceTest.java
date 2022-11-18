package com.smartsparrow.eval.service;

import static com.smartsparrow.eval.action.progress.ProgressionType.INTERACTIVE_COMPLETE;
import static com.smartsparrow.eval.action.progress.ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO;
import static com.smartsparrow.eval.action.progress.ProgressionType.INTERACTIVE_REPEAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.CoursewareHistoryService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class InteractiveProgressUpdateServiceTest {

    @InjectMocks
    private InteractiveProgressUpdateService progressUpdateService;

    @Mock
    private ProgressService progressService;

    @Mock
    private CoursewareHistoryService coursewareHistoryService;

    @Mock
    private LearnerEvaluationResponseContext responseContext;

    @Mock
    private ProgressAction action;
    @Mock
    private ProgressActionContext actionContext;
    @Mock
    private LearnerEvaluationResponse evaluationResponse;
    @Mock
    private WalkableEvaluationResult walkableEvaluationResult;


    public static final UUID elementId = UUID.randomUUID();
    public static final UUID attemptId = UUID.randomUUID();
    public static final UUID changeId = UUID.randomUUID();
    public static final UUID deploymentId = UUID.randomUUID();
    public static final UUID evaluationId = UUID.randomUUID();
    public static final UUID studentId = UUID.randomUUID();
    public static final UUID learnerPathwayId = UUID.randomUUID();

    private final CoursewareElement element = new CoursewareElement()
            .setElementType(CoursewareElementType.INTERACTIVE)
            .setElementId(elementId);

    private final LearnerWalkable walkable = new LearnerInteractive()
            .setId(elementId)
            .setChangeId(changeId)
            .setDeploymentId(deploymentId);

    private final Attempt attempt = new Attempt()
            .setId(attemptId)
            .setStudentId(studentId)
            .setCoursewareElementId(elementId)
            .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
            .setDeploymentId(deploymentId)
            .setValue(1)
            .setParentId(learnerPathwayId);

    private final Deployment deployment = new Deployment()
            .setId(deploymentId)
            .setChangeId(changeId);

    private final LearnerEvaluationRequest request = new LearnerEvaluationRequest()
            .setAttempt(attempt)
            .setDeployment(deployment)
            .setLearnerWalkable(walkable)
            .setParentPathwayId(learnerPathwayId)
            .setStudentId(studentId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(action.getContext()).thenReturn(actionContext);

        when(progressService.persist(any(Progress.class))).thenReturn(Flux.just(new Void[]{}));

        when(coursewareHistoryService.record(any(UUID.class), any(LearnerEvaluationRequest.class), any(CoursewareElement.class), any(Attempt.class), any(UUID.class)))
                .thenReturn(Mono.just(new CompletedWalkable()));
        when(responseContext.getResponse()).thenReturn(evaluationResponse);
        when(evaluationResponse.getWalkableEvaluationResult()).thenReturn(walkableEvaluationResult);
    }

    @Test
    void updateProgress_interactiveComplete() {
        when(actionContext.getProgressionType()).thenReturn(INTERACTIVE_COMPLETE);
        when(evaluationResponse.getEvaluationRequest()).thenReturn(request);
        when(walkableEvaluationResult.getId()).thenReturn(evaluationId);
        progressUpdateService.updateProgress(element, action, responseContext).block();

        ArgumentCaptor<Progress> persistCaptor = ArgumentCaptor.forClass(Progress.class);

        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getValue());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getConfidence());

        verify(coursewareHistoryService)
                .record(eq(evaluationId), any(LearnerEvaluationRequest.class), any(CoursewareElement.class), any(Attempt.class), any(UUID.class));
    }

    @Test
    void updateProgress_interactiveIncomplete() {
        when(actionContext.getProgressionType()).thenReturn(INTERACTIVE_REPEAT);
        when(evaluationResponse.getEvaluationRequest()).thenReturn(request);
        when(walkableEvaluationResult.getId()).thenReturn(evaluationId);
        progressUpdateService.updateProgress(element, action, responseContext).block();

        ArgumentCaptor<Progress> persistCaptor = ArgumentCaptor.forClass(Progress.class);

        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(0f), persistCaptor.getValue().getCompletion().getValue());
        assertEquals(Float.valueOf(0.19999999f), persistCaptor.getValue().getCompletion().getConfidence());

        verify(coursewareHistoryService, never()).record(any(UUID.class), any(EvaluationResult.class), any(CoursewareElementType.class));
    }

    @Test
    void updateProgress_completeInteractiveAndGoTo() {
        when(actionContext.getProgressionType()).thenReturn(INTERACTIVE_COMPLETE_AND_GO_TO);
        when(evaluationResponse.getEvaluationRequest()).thenReturn(request);
        when(walkableEvaluationResult.getId()).thenReturn(evaluationId);
        progressUpdateService.updateProgress(element, action, responseContext).block();

        ArgumentCaptor<Progress> persistCaptor = ArgumentCaptor.forClass(Progress.class);

        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getValue());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getConfidence());

        verify(coursewareHistoryService)
                .record(eq(evaluationId), any(LearnerEvaluationRequest.class), any(CoursewareElement.class), any(Attempt.class), any(UUID.class));
    }
}
