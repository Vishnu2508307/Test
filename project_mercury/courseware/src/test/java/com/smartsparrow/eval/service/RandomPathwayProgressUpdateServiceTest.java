package com.smartsparrow.eval.service;

import static com.smartsparrow.courseware.pathway.WalkableChildStub.buildWalkableChild;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerRandomPathway;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.progress.RandomPathwayProgress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RandomPathwayProgressUpdateServiceTest {

    public static final UUID elementId = UUID.randomUUID();
    public static final UUID attemptId = UUID.randomUUID();
    public static final UUID studentId = UUID.randomUUID();
    public static final UUID learnerPathwayId = UUID.randomUUID();
    public static final UUID walkableId = UUID.randomUUID();
    private static final UUID pathwayId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID changeId = UUIDs.timeBased();
    private static final WalkableChild walkableOne = buildWalkableChild();
    private static final WalkableChild walkableTwo = buildWalkableChild();
    private static final WalkableChild walkableThree = buildWalkableChild();
    private static final WalkableChild walkableFour = buildWalkableChild();
    private final List<Progress> progresses = new ArrayList<>();
    private final Deployment deployment = new Deployment()
            .setId(deploymentId)
            .setChangeId(changeId);
    private final LearnerWalkable walkable = new LearnerInteractive()
            .setId(elementId)
            .setChangeId(changeId)
            .setDeploymentId(deploymentId);
    final Map<UUID, Float> childCompletionValues = new HashMap<>() {
        {
            put(walkableThree.getElementId(), 1f);
        }
    };
    final Map<UUID, Float> childCompletionConfidences = new HashMap<>() {
        {
            put(walkableThree.getElementId(), 1f);
        }
    };
    final RandomPathwayProgress randomPathwayProgress = new RandomPathwayProgress()
            .setCompletedWalkables(Lists.newArrayList(walkableThree.getElementId()))
            .setChildWalkableCompletionValues(childCompletionValues)
            .setChildWalkableCompletionConfidences(childCompletionConfidences);
    @InjectMocks
    private RandomPathwayProgressUpdateService randomPathwayProgressUpdateService;
    @Mock
    private ProgressAction action;
    @Mock
    private ProgressService progressService;
    @Mock
    private LearnerPathwayService learnerPathwayService;
    @Mock
    private Progress progress;
    @Mock
    private LearnerEvaluationResponseContext responseContext;
    @Mock
    private LearnerEvaluationResponse evaluationResponse;
    @Mock
    private WalkableEvaluationResult walkableEvaluationResult;
    @Mock
    private AttemptService attemptService;
    @Mock
    private Completion completion;
    private LearnerRandomPathway learnerRandomPathway;
    private final Attempt attempt = new Attempt()
            .setId(attemptId)
            .setStudentId(studentId)
            .setCoursewareElementId(elementId)
            .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
            .setDeploymentId(deploymentId)
            .setValue(1)
            .setParentId(learnerPathwayId);
    private final LearnerEvaluationRequest request = new LearnerEvaluationRequest()
            .setAttempt(attempt)
            .setDeployment(deployment)
            .setLearnerWalkable(walkable)
            .setParentPathwayId(learnerPathwayId)
            .setStudentId(studentId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        progresses.add(progress);
        learnerRandomPathway = new LearnerRandomPathway(progressService, learnerPathwayService);
        learnerRandomPathway
                .setId(pathwayId)
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
                .setConfig("{\"exitAfter\": 2}");
        when(responseContext.getProgresses()).thenReturn(progresses);
        when(responseContext.getResponse()).thenReturn(evaluationResponse);
        when(evaluationResponse.getWalkableEvaluationResult()).thenReturn(walkableEvaluationResult);
        when(evaluationResponse.getEvaluationRequest()).thenReturn(request);
        when(evaluationResponse.getWalkableEvaluationResult().getId()).thenReturn(walkableId);
        when(attemptService.findById(responseContext.getProgresses().get(0).getAttemptId())).thenReturn(Mono.just(
                attempt));
        when(progressService.findLatestRandomPathway(deploymentId, pathwayId, studentId)).thenReturn(Mono.just(
                randomPathwayProgress));
        when(learnerPathwayService.findWalkables(pathwayId, deploymentId)).thenReturn(Flux.just(
                walkableOne,
                walkableTwo,
                walkableThree,
                walkableFour
        ));


        when(progress.getCompletion()).thenReturn(completion);
        when(progress.getCompletion().getValue()).thenReturn(1f);

        when(progressService.persist(any(RandomPathwayProgress.class))).thenReturn(Flux.just(new Void[]{}));

    }

    @Test
    void updateProgress() {
        Progress response = randomPathwayProgressUpdateService.updateProgress(learnerRandomPathway,
                                                                              action,
                                                                              responseContext).block();
        ArgumentCaptor<RandomPathwayProgress> persistCaptor = ArgumentCaptor.forClass(RandomPathwayProgress.class);
        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getValue());
        assertNotNull(response);
        assertEquals(studentId, response.getStudentId());
        assertEquals(deploymentId, response.getDeploymentId());
        assertEquals(walkableEvaluationResult.getId(), response.getEvaluationId());
    }
}
