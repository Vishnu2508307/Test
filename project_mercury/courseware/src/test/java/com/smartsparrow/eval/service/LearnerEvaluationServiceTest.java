package com.smartsparrow.eval.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.EvaluationContext;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.LearnerScenarioService;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerEvaluationServiceTest {

    @InjectMocks
    private LearnerEvaluationService learnerEvaluationService;

    @Mock
    private LearnerScenarioService learnerScenarioService;

    @Mock
    private ScenarioEvaluationService scenarioEvaluationService;

    @Mock
    private StudentScopeService studentScopeService;

    @Mock
    LearnerWalkable learnerWalkable;

    private DeployedActivity deployedActivity;
    private LearnerEvaluationRequest learnerEvaluationRequest;

    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID walkableId = UUIDs.timeBased();
    private static final UUID studentScopeUrn = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();
    private static final UUID attemptId = UUIDs.timeBased();
    private static final UUID parentAttemptId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID changeId = UUIDs.timeBased();
    private static final UUID scenarioIdOne = UUIDs.timeBased();
    private static final UUID scenarioIdOTwo = UUIDs.timeBased();
    private static final String clientId = "clientId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(learnerWalkable.getId()).thenReturn(walkableId);
        when(learnerWalkable.getEvaluationMode()).thenReturn(EvaluationMode.DEFAULT);
        when(learnerWalkable.getDeploymentId()).thenReturn(deploymentId);
        when(learnerWalkable.getChangeId()).thenReturn(changeId);
        when(learnerWalkable.getStudentScopeURN()).thenReturn(studentScopeUrn);
        when(learnerWalkable.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);

        LearnerScenario one = new LearnerScenario()
                .setId(scenarioIdOne);

        LearnerScenario two = new LearnerScenario()
                .setId(scenarioIdOTwo);

        deployedActivity = new DeployedActivity()
                .setId(deploymentId)
                .setChangeId(changeId);

        learnerEvaluationRequest = new LearnerEvaluationRequest()
                .setDeployment(deployedActivity)
                .setLearnerWalkable(learnerWalkable)
                .setAttempt(new Attempt()
                        .setId(attemptId)
                        .setParentId(parentAttemptId))
                .setStudentId(studentId)
                .setProducingClientId(clientId)
                .setParentPathwayId(parentPathwayId)
                .setScenarioLifecycle(ScenarioLifecycle.INTERACTIVE_EVALUATE);

        when(learnerScenarioService.findAll(deploymentId, changeId, walkableId, ScenarioLifecycle.INTERACTIVE_EVALUATE))
                .thenReturn(Flux.just(one, two));
        when(scenarioEvaluationService.evaluateCondition(eq(one), any(EvaluationLearnerContext.class)))
                .thenReturn(Mono.just(new ScenarioEvaluationResult()
                        .setScenarioId(scenarioIdOne)
                        .setEvaluationResult(true)
                        .setScenarioCorrectness(ScenarioCorrectness.incorrect)));
        when(scenarioEvaluationService.evaluateCondition(eq(two), any(EvaluationLearnerContext.class)))
                .thenReturn(Mono.just(new ScenarioEvaluationResult()
                        .setScenarioId(scenarioIdOTwo)
                        .setEvaluationResult(false)
                        .setScenarioCorrectness(ScenarioCorrectness.correct)));
        when(studentScopeService.findLatestEntries(deploymentId, studentId, studentScopeUrn))
                .thenReturn(Mono.just(new HashMap<>()));
    }

    @Test
    void evaluate_defaultMode() {
        ArgumentCaptor<EvaluationLearnerContext> contextCaptor = ArgumentCaptor.forClass(EvaluationLearnerContext.class);

        LearnerEvaluationResponse response = learnerEvaluationService.evaluate(learnerEvaluationRequest)
                .block();

        assertNotNull(response);
        assertEquals(learnerEvaluationRequest, response.getEvaluationRequest());
        assertEquals(1, response.getScenarioEvaluationResults().size());

        verify(scenarioEvaluationService, times(1))
                .evaluateCondition(any(LearnerScenario.class), contextCaptor.capture());

        WalkableEvaluationResult evaluation = response.getWalkableEvaluationResult();

        assertNotNull(evaluation);
        assertEquals(walkableId, evaluation.getWalkableId());
        assertEquals(CoursewareElementType.INTERACTIVE, evaluation.getWalkableType());
        assertEquals(ScenarioCorrectness.incorrect, evaluation.getTruthfulScenario().getScenarioCorrectness());
        assertNotNull(evaluation.getId());
        assertFalse(evaluation.isWalkableComplete());
        assertNotNull(evaluation.getTriggeredActions());
        assertEquals(0, evaluation.getTriggeredActions().size());;

        EvaluationLearnerContext context = contextCaptor.getValue();
        assertNotNull(context);
        assertEquals(deploymentId, context.getDeploymentId());
        assertEquals(studentId, context.getStudentId());
        assertEquals(EvaluationContext.Type.LEARNER, context.getType());
    }

    @Test
    void evaluate_combinedMode() {
        when(learnerWalkable.getEvaluationMode()).thenReturn(EvaluationMode.COMBINED);

        ArgumentCaptor<EvaluationLearnerContext> contextCaptor = ArgumentCaptor.forClass(EvaluationLearnerContext.class);

        LearnerEvaluationResponse response = learnerEvaluationService.evaluate(learnerEvaluationRequest)
                .block();

        assertNotNull(response);
        assertEquals(learnerEvaluationRequest, response.getEvaluationRequest());
        assertEquals(2, response.getScenarioEvaluationResults().size());

        verify(scenarioEvaluationService, times(2))
                .evaluateCondition(any(LearnerScenario.class), contextCaptor.capture());

        WalkableEvaluationResult evaluation = response.getWalkableEvaluationResult();

        assertNotNull(evaluation);
        assertEquals(walkableId, evaluation.getWalkableId());
        assertEquals(CoursewareElementType.INTERACTIVE, evaluation.getWalkableType());
        assertEquals(ScenarioCorrectness.incorrect, evaluation.getTruthfulScenario().getScenarioCorrectness());
        assertNotNull(evaluation.getId());
        assertFalse(evaluation.isWalkableComplete());
        assertNotNull(evaluation.getTriggeredActions());
        assertEquals(0, evaluation.getTriggeredActions().size());

        EvaluationLearnerContext context = contextCaptor.getValue();
        assertNotNull(context);
        assertEquals(deploymentId, context.getDeploymentId());
        assertEquals(studentId, context.getStudentId());
        assertEquals(EvaluationContext.Type.LEARNER, context.getType());
    }

}