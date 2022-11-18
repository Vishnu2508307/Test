package com.smartsparrow.eval.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.eval.wiring.EvaluationFeatureConfig;
import com.smartsparrow.eval.wiring.EvaluationFeatureConfigurationValues;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.service.EvaluationRequestService;
import com.smartsparrow.learner.service.LearnerWalkableService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

class EvaluationServiceAdapterTest {

    @InjectMocks
    private EvaluationServiceAdapter evaluationServiceAdapter;

    @Mock
    private Provider<EvaluationFeatureConfig> evaluationFeatureConfigProvider;

    @Mock
    private LearnerWalkableService learnerWalkableService;

    @Mock
    private EvaluationRequestService evaluationRequestService;

    @Mock
    private EvaluationFeatureConfig globalConfig;

    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID studentId = UUIDs.timeBased();
    private static final String clientId = "clientId";
    private static final UUID timeId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(evaluationFeatureConfigProvider.get()).thenReturn(globalConfig);
        when(learnerWalkableService.evaluate(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE, studentId, clientId))
                .thenReturn(Mono.just(new LearnerEvaluationResponse()
                        .setScenarioEvaluationResults(new ArrayList<>())
                        .setWalkableEvaluationResult(new WalkableEvaluationResult()
                                .setTruthfulScenario(new ScenarioEvaluationResult()
                                        .setScenarioCorrectness(ScenarioCorrectness.correct)))
                        .setEvaluationRequest(new LearnerEvaluationRequest()
                                .setAttempt(new Attempt()
                                        .setId(UUIDs.timeBased()))
                                .setLearnerWalkable(new LearnerInteractive()))));
        when(learnerWalkableService.evaluate(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE, studentId, clientId, timeId))
                .thenReturn(Mono.just(new LearnerEvaluationResponse()
                                              .setScenarioEvaluationResults(new ArrayList<>())
                                              .setWalkableEvaluationResult(new WalkableEvaluationResult()
                                                                                   .setTruthfulScenario(new ScenarioEvaluationResult()
                                                                                                                .setScenarioCorrectness(ScenarioCorrectness.correct)))
                                              .setEvaluationRequest(new LearnerEvaluationRequest()
                                                                            .setAttempt(new Attempt()
                                                                                                .setId(UUIDs.timeBased()))
                                                                            .setLearnerWalkable(new LearnerInteractive()))));
        when(evaluationRequestService.evaluate(deploymentId, interactiveId, clientId, studentId))
                .thenReturn(Mono.just(new EvaluationResult()));
    }

    @Test
    void evaluate_nullName() {
        when(globalConfig.getConfiguredFeature()).thenReturn(EvaluationFeatureConfigurationValues.CAMEL_EVALUATION);

        EvaluationResult result = evaluationServiceAdapter.evaluate(deploymentId, interactiveId, clientId, studentId, null)
                .block();

        assertNotNull(result);

        verify(evaluationRequestService).evaluate(deploymentId, interactiveId, clientId, studentId);
        verify(learnerWalkableService, never()).evaluate(any(UUID.class), any(UUID.class), any(CoursewareElementType.class), any(UUID.class),
                anyString());
    }

    @Test
    void evaluate_invalidName() {
        when(globalConfig.getConfiguredFeature()).thenReturn(EvaluationFeatureConfigurationValues.REACTIVE_EVALUATION);

        EvaluationResult result = evaluationServiceAdapter.evaluate(deploymentId, interactiveId, clientId, studentId, AccountShadowAttributeName.UNIVERSITY_COURSE_NAME)
                .block();

        assertNotNull(result);

        verify(evaluationRequestService, never()).evaluate(deploymentId, interactiveId, clientId, studentId);
        verify(learnerWalkableService).evaluate(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE, studentId, clientId);
    }

    @Test
    void evaluate_validName() {
        EvaluationResult result = evaluationServiceAdapter.evaluate(deploymentId, interactiveId, clientId, studentId, AccountShadowAttributeName.REACTIVE_EVALUATION)
                .block();

        assertNotNull(result);

        verify(evaluationRequestService, never()).evaluate(deploymentId, interactiveId, clientId, studentId);
        verify(learnerWalkableService).evaluate(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE, studentId, clientId);
    }


    @Test
    void evaluate_timeId() {
        EvaluationResult result = evaluationServiceAdapter.evaluate(deploymentId, interactiveId, clientId, studentId,
                                                                    AccountShadowAttributeName.REACTIVE_EVALUATION, timeId)
                .block();

        assertNotNull(result);

        verify(evaluationRequestService, never()).evaluate(deploymentId, interactiveId, clientId, studentId);
        verify(learnerWalkableService).evaluate(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE, studentId, clientId, timeId);
    }
}
