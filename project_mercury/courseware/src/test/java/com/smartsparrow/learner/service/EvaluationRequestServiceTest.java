package com.smartsparrow.learner.service;

import static com.smartsparrow.dataevent.RouteUri.LEARNER_EVALUATE_COMPLETE;
import static junit.framework.TestCase.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.lang.ScenarioEvaluationException;
import com.smartsparrow.eval.service.ScenarioEvaluationService;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.lang.LearnerEvaluationException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class EvaluationRequestServiceTest {

    @InjectMocks
    private EvaluationRequestService evaluationRequestService;

    @Mock
    private CamelReactiveStreamsService camel;
    TestPublisher<EvaluationEventMessage> publisher;

    @Mock
    private LearnerCoursewareService learnerCoursewareService;

    @Mock
    private ScenarioEvaluationService scenarioEvaluationService;

    @Mock
    private LearnerScenarioService learnerScenarioService;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private AcquireAttemptService acquireAttemptService;

    @Mock
    private EvaluationResultService evaluationResultService;

    @Mock
    private LearnerInteractiveService learnerInteractiveService;

    @Mock
    private StudentScopeService studentScopeService;

    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID scenarioIdCorrect = UUID.randomUUID();
    private static final UUID scenarioIdIncorrect = UUID.randomUUID();
    private static final UUID scenarioIdNone = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID attemptId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final UUID parentPathwayId = UUID.randomUUID();
    private static final UUID parentPathwayAttemptId = UUID.randomUUID();
    private static final String producingClientId = "clientId";
    private static final String changeProgressActionsComplete = "CHANGE_PROGRESS";
    private static final String changeProgressActionsIncomplete = "CHANGE_PROGRESS_INCOMPLETE";
    private static final String changeScopeActions = "CHANGE_SCOPE";
    private CoursewareElement interactiveElement;
    private CoursewareElement parentPathwayElement;
    private CoursewareElement parentActivityElement;
    private LearnerScenario correct;
    private LearnerScenario incorrect;
    private LearnerScenario none;
    private DeployedActivity deployment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        correct = new LearnerScenario()
                .setId(scenarioIdCorrect)
                .setCorrectness(ScenarioCorrectness.correct);

        incorrect = new LearnerScenario()
                .setId(scenarioIdIncorrect)
                .setCorrectness(ScenarioCorrectness.incorrect);

        none = new LearnerScenario()
                .setId(scenarioIdNone)
                .setCorrectness(ScenarioCorrectness.none);

        deployment = new DeployedActivity()
                .setId(deploymentId)
                .setChangeId(changeId)
                .setCohortId(cohortId);

        interactiveElement = mock(CoursewareElement.class);
        parentPathwayElement = mock(CoursewareElement.class);
        parentActivityElement = mock(CoursewareElement.class);

        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));

        when(learnerScenarioService.findAll(any(Deployment.class), eq(interactiveId), eq(ScenarioLifecycle.INTERACTIVE_EVALUATE)))
                .thenReturn(Flux.just(
                        correct,
                        incorrect,
                        none
                ));

        when(learnerCoursewareService.getAncestry(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE))
                .thenReturn(Mono.just(Lists.newArrayList(
                        interactiveElement,
                        parentPathwayElement,
                        parentActivityElement
                )));
        publisher = TestPublisher.create();
        when(camel.toStream(eq(LEARNER_EVALUATE_COMPLETE), any(EvaluationEventMessage.class), eq(EvaluationEventMessage.class)))
                .thenReturn(publisher);

        when(acquireAttemptService.acquireLatestInteractiveAttempt(deploymentId, interactiveId, studentId))
                .thenReturn(Mono.just(new Attempt().setId(attemptId)));

        when(learnerInteractiveService.findInteractive(interactiveId, deploymentId))
                .thenReturn(Mono.just(new LearnerInteractive()
                        .setStudentScopeURN(studentScopeURN)
                        .setId(interactiveId)
                        .setEvaluationMode(EvaluationMode.DEFAULT)));

        when(learnerInteractiveService.findParentPathwayId(interactiveId, deploymentId))
                .thenReturn(Mono.just(parentPathwayId));

        when(acquireAttemptService.acquireLatestInteractiveAttempt(deploymentId, interactiveId, studentId))
                .thenReturn(Mono.just(new Attempt().setId(attemptId).setParentId(parentPathwayAttemptId)));

        when(studentScopeService.findLatestEntries(deploymentId, studentId, studentScopeURN))
                .thenReturn(Mono.just(new HashMap<>()));

        when(evaluationResultService.persist(any(Evaluation.class))).thenReturn(Mono.just(new Evaluation()));
    }

    @Test
    @DisplayName("It should throw an iae when deploymentId is missing")
    void evaluate_nullDeploymentId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> evaluationRequestService.evaluate(null, interactiveId, producingClientId, studentId).block());

        assertEquals("deploymentId is required", e.getMessage());
    }

    @Test
    @DisplayName("It should throw an iae when interactiveId is missing")
    void evaluate_nullInteractiveId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> evaluationRequestService.evaluate(deploymentId, null, producingClientId, studentId).block());

        assertEquals("interactiveId is required", e.getMessage());
    }

    @Test
    @DisplayName("It should throw an iae when producingClientId is missing")
    void evaluate_nullProducingclientId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> evaluationRequestService.evaluate(deploymentId, interactiveId, null, studentId).block());

        assertEquals("producingClientId is required", e.getMessage());
    }

    @Test
    @DisplayName("It should throw an iae when studentId is missing")
    void evaluate_nullStudentId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> evaluationRequestService.evaluate(deploymentId, interactiveId, producingClientId, null).block());

        assertEquals("studentId is required", e.getMessage());
    }

    @Test
    @DisplayName("It should throw a LearnerEvaluateException when the deployment is not found")
    void evaluate_noDeployment() {
        TestPublisher<DeployedActivity> deploymentTestPublisher = TestPublisher.create();
        deploymentTestPublisher.error(new DeploymentNotFoundException(null, deploymentId));

        when(deploymentService.findDeployment(deploymentId)).thenReturn(deploymentTestPublisher.mono());

        assertThrows(LearnerEvaluationException.class,
                () -> evaluationRequestService.evaluate(deploymentId, interactiveId, producingClientId, studentId).block());
    }

    @Test
    @DisplayName("It should throw an exception when failing to evaluate a scenario")
    void evaluate_ScenarioEvaluationException() {
        TestPublisher<ScenarioEvaluationResult> publisher = TestPublisher.create();
        publisher.error(new ScenarioEvaluationException(null, null));

        when(scenarioEvaluationService.evaluateCondition(eq(correct), any(EvaluationLearnerContext.class))).thenReturn(publisher.mono());

        assertThrows(LearnerEvaluationException.class,
                () -> evaluationRequestService.evaluate(deploymentId, interactiveId, producingClientId, studentId).block());
    }

    @Test
    @DisplayName("It should set interactiveComplete to false when a CHANGE_PROGESS action with INTERACTIVE_REPEAT progression type is triggered")
    void evaluate_interactiveComplete_REPEAT() {
        ScenarioEvaluationResult res1 = buildScenarioEvaluationResult(false, correct, changeProgressActionsIncomplete);
        ScenarioEvaluationResult res2 = buildScenarioEvaluationResult(false, incorrect, changeProgressActionsIncomplete);
        ScenarioEvaluationResult res3 = buildScenarioEvaluationResult(true, none, changeProgressActionsIncomplete);

        when(scenarioEvaluationService.evaluateCondition(eq(correct), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res1));
        when(scenarioEvaluationService.evaluateCondition(eq(incorrect), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res2));
        when(scenarioEvaluationService.evaluateCondition(eq(none), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res3));

        EvaluationResult evaluationResult = getEvaluationResultFromCamelInput(deploymentId, interactiveId,
                producingClientId, studentId);

        assertNotNull(evaluationResult);
        assertFalse(evaluationResult.getInteractiveComplete());
        assertEquals(interactiveId, evaluationResult.getCoursewareElementId());
        assertEquals(none.getCorrectness(), evaluationResult.getScenarioCorrectness());
        assertEquals(3, evaluationResult.getScenarioEvaluationResults().size());
        assertNotNull(evaluationResult.getId());

        assertEquals(cohortId, evaluationResult.getDeployment().getCohortId());
        assertEquals(deploymentId, evaluationResult.getDeployment().getId());
        assertEquals(changeId, evaluationResult.getDeployment().getChangeId());
        assertEquals(attemptId, evaluationResult.getAttemptId());
    }

    @Test
    @DisplayName("It should set interactiveComplete to false when a non CHANGE_PROGESS action type is triggered")
    void evaluate_interactiveComplete_nonChangeProgress() {
        ScenarioEvaluationResult res1 = buildScenarioEvaluationResult(false, correct, changeScopeActions);
        ScenarioEvaluationResult res2 = buildScenarioEvaluationResult(false, incorrect, changeScopeActions);
        ScenarioEvaluationResult res3 = buildScenarioEvaluationResult(false, none, changeScopeActions);

        when(scenarioEvaluationService.evaluateCondition(eq(correct), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res1));
        when(scenarioEvaluationService.evaluateCondition(eq(incorrect), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res2));
        when(scenarioEvaluationService.evaluateCondition(eq(none), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res3));

        EvaluationResult evaluationResult = getEvaluationResultFromCamelInput(deploymentId, interactiveId,
                producingClientId, studentId);

        assertNotNull(evaluationResult);
        assertFalse(evaluationResult.getInteractiveComplete());
        assertEquals(interactiveId, evaluationResult.getCoursewareElementId());
        assertNull(evaluationResult.getScenarioCorrectness());
        assertEquals(3, evaluationResult.getScenarioEvaluationResults().size());
        assertNotNull(evaluationResult.getId());

        assertEquals(cohortId, evaluationResult.getDeployment().getCohortId());
        assertEquals(deploymentId, evaluationResult.getDeployment().getId());
        assertEquals(changeId, evaluationResult.getDeployment().getChangeId());
        assertEquals(attemptId, evaluationResult.getAttemptId());
    }

    @Test
    @DisplayName("It should set interactiveComplete to false when a CHANGE_PROGESS action type is not triggered")
    void evaluate_interactiveComplete_scenarioIsFalse() {
        ScenarioEvaluationResult res1 = buildScenarioEvaluationResult(false, correct, changeProgressActionsComplete);
        ScenarioEvaluationResult res2 = buildScenarioEvaluationResult(false, incorrect, changeProgressActionsComplete);
        ScenarioEvaluationResult res3 = buildScenarioEvaluationResult(false, none, changeProgressActionsComplete);

        when(scenarioEvaluationService.evaluateCondition(eq(correct), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res1));
        when(scenarioEvaluationService.evaluateCondition(eq(incorrect), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res2));
        when(scenarioEvaluationService.evaluateCondition(eq(none), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(res3));

        EvaluationResult evaluationResult = getEvaluationResultFromCamelInput(deploymentId, interactiveId,
                producingClientId, studentId);

        assertNotNull(evaluationResult);
        assertFalse(evaluationResult.getInteractiveComplete());
        assertEquals(interactiveId, evaluationResult.getCoursewareElementId());
        assertNull(evaluationResult.getScenarioCorrectness());
        assertEquals(3, evaluationResult.getScenarioEvaluationResults().size());
        assertNotNull(evaluationResult.getId());

        assertEquals(cohortId, evaluationResult.getDeployment().getCohortId());
        assertEquals(deploymentId, evaluationResult.getDeployment().getId());
        assertEquals(changeId, evaluationResult.getDeployment().getChangeId());

        assertEquals(attemptId, evaluationResult.getAttemptId());
    }

    private EvaluationResult getEvaluationResultFromCamelInput(UUID deploymentId, UUID interactiveId,
                                                               String producingClientId, UUID studentId) {
        publisher.emit(new EvaluationEventMessage());
        evaluationRequestService.evaluate(deploymentId, interactiveId, producingClientId, studentId).block();

        ArgumentCaptor<EvaluationEventMessage> evaluationEventMessageCaptor = ArgumentCaptor
                .forClass(EvaluationEventMessage.class);
        verify(camel).toStream(eq(LEARNER_EVALUATE_COMPLETE), evaluationEventMessageCaptor.capture(),
                eq(EvaluationEventMessage.class));
        return evaluationEventMessageCaptor.getValue().getEvaluationResult();
    }


    private ScenarioEvaluationResult buildScenarioEvaluationResult(boolean evaluation, Scenario scenario, String actions) {
        return new ScenarioEvaluationResult()
                .setActions(actions)
                .setEvaluationResult(evaluation)
                .setScenarioCorrectness(scenario.getCorrectness())
                .setScenarioId(scenario.getId());
    }

}
