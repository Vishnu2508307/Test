package com.smartsparrow.eval.service;

import static com.smartsparrow.eval.action.progress.ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.FreeLearnerPathway;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.FreePathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class FreePathwayProgressUpdateServiceTest {


    public static final UUID elementId = UUID.randomUUID();
    public static final UUID attemptId = UUID.randomUUID();
    public static final UUID changeId = UUID.randomUUID();
    public static final UUID deploymentId = UUID.randomUUID();
    public static final UUID studentId = UUID.randomUUID();
    public static final UUID pathwayId = UUID.randomUUID();
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
            .setParentId(pathwayId);
    private final Deployment deployment = new Deployment()
            .setId(deploymentId)
            .setChangeId(changeId);
    private final LearnerEvaluationRequest request = new LearnerEvaluationRequest()
            .setAttempt(attempt)
            .setDeployment(deployment)
            .setLearnerWalkable(walkable)
            .setParentPathwayId(pathwayId)
            .setStudentId(studentId);
    private final List<Progress> progresses = new ArrayList<>();
    @InjectMocks
    private FreePathwayProgressUpdateService updateService;
    @Mock
    private ProgressService progressService;
    @Mock
    private AttemptService attemptService;
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
    @Mock
    private FreeLearnerPathway freeLearnerPathway;
    @Mock
    private Progress progress;
    @Mock
    private EvaluationActionState evaluationActionState;
    @Mock
    private LearnerPathwayService learnerPathwayService;
    @Mock
    private Completion completion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        progresses.add(progress);

        when(action.getContext()).thenReturn(actionContext);
        when(responseContext.getResponse()).thenReturn(evaluationResponse);
        when(evaluationResponse.getWalkableEvaluationResult()).thenReturn(walkableEvaluationResult);
        when(evaluationResponse.getEvaluationRequest()).thenReturn(request);
        when(freeLearnerPathway.getId()).thenReturn(pathwayId);
        when(freeLearnerPathway.getDeploymentId()).thenReturn(deploymentId);
        when(responseContext.getProgresses()).thenReturn(progresses);
        when(progress.getAttemptId()).thenReturn(attemptId);

        when(progress.getCompletion()).thenReturn(completion);
        when(responseContext.getEvaluationActionState()).thenReturn(evaluationActionState);
        when(evaluationActionState.getCoursewareElement()).thenReturn(element);
        when(evaluationActionState.getProgressActionContext()).thenReturn(actionContext);

        when(progress.getCompletion().getValue()).thenReturn(1f);
        when(progress.getCompletion().getConfidence()).thenReturn(1f);
        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(new Attempt().setParentId(UUID.randomUUID())));
        when(progressService.findLatestFreePathway(any(UUID.class),
                                                   any(UUID.class),
                                                   any(UUID.class))).thenReturn(Mono.empty());
        when(learnerPathwayService.findWalkables(any(UUID.class), any(UUID.class))).thenReturn(Flux.empty());
        when(progressService.persist(any(FreePathwayProgress.class))).thenReturn(Flux.just(new Void[]{}));

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
        FreePathwayProgress freePathwayProgress = new FreePathwayProgress()
                .setChildWalkableCompletionValues(new HashMap<>() {
                    {
                        put(interactiveId, 0.5f);
                    }
                })
                .setChildWalkableCompletionConfidences(new HashMap<>() {
                    {
                        put(interactiveId, 0.5f);
                    }
                });
        when(learnerPathwayService.findWalkables(pathwayId, deploymentId)).thenReturn(Flux.just(child));
        when(progressService.findLatestFreePathway(deploymentId, pathwayId, studentId)).thenReturn(Mono.just(
                freePathwayProgress));
        updatePathwayProgressCommonTest();

    }

    @Test
    void updateProgress_interactiveCompleteAndPathwayComplete() {
        when(actionContext.getProgressionType()).thenReturn(INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE);

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(attempt));

        ArgumentCaptor<FreePathwayProgress> freePathwayProgressCaptor = ArgumentCaptor.forClass(FreePathwayProgress.class);
        updateService.updateProgress(freeLearnerPathway, action, responseContext).block();
        verify(progressService).persist(freePathwayProgressCaptor.capture());
        FreePathwayProgress persistedProgress = freePathwayProgressCaptor.getValue();

        assertNotNull(persistedProgress);
        assertEquals(Float.valueOf(1f), persistedProgress.getCompletion().getValue());
        assertEquals(Float.valueOf(1f), persistedProgress.getCompletion().getConfidence());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(pathwayId, persistedProgress.getCoursewareElementId());
        assertEquals(studentId, persistedProgress.getStudentId());
        assertEquals(attempt.getParentId(), persistedProgress.getAttemptId());
        assertNotNull(persistedProgress.getId());
        assertEquals(deploymentId, persistedProgress.getDeploymentId());
        assertEquals(changeId, persistedProgress.getChangeId());

    }


    void updatePathwayProgressCommonTest() {
        when(actionContext.getProgressionType()).thenReturn(INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE);
        updateService.updateProgress(freeLearnerPathway, action, responseContext).block();
        ArgumentCaptor<FreePathwayProgress> persistCaptor = ArgumentCaptor.forClass(FreePathwayProgress.class);
        verify(progressService).persist(persistCaptor.capture());
        FreePathwayProgress capturedPersist = persistCaptor.getValue();


        assertEquals(deploymentId, capturedPersist.getDeploymentId());
        assertEquals(pathwayId, capturedPersist.getCoursewareElementId());
        assertEquals(studentId, capturedPersist.getStudentId());
        assertNotNull(capturedPersist);
        assertEquals(Float.valueOf(1.0f), capturedPersist.getCompletion().getValue());
        assertEquals(Float.valueOf(1.0f), capturedPersist.getCompletion().getConfidence());
    }
}