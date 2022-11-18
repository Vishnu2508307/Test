package com.smartsparrow.eval.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.eval.wiring.EvaluationFeatureConfig;
import com.smartsparrow.eval.wiring.EvaluationFeatureConfigurationValues;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.service.EvaluationRequestService;
import com.smartsparrow.learner.service.LearnerWalkableService;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

/**
 * This class is responsible for invoking the learner evaluation implementation based on global and account configurations.
 * It will either invoke {@link com.smartsparrow.learner.service.LearnerWalkableService#evaluate(UUID, UUID, CoursewareElementType, UUID, String)}
 * or {@link com.smartsparrow.learner.service.EvaluationRequestService#evaluate(UUID, UUID, String, UUID)}
 */
@Singleton
public class EvaluationServiceAdapter {

    private final Provider<EvaluationFeatureConfig> evaluationFeatureConfigProvider;
    private final LearnerWalkableService learnerWalkableService;
    private final EvaluationRequestService evaluationRequestService;

    @Inject
    public EvaluationServiceAdapter(final Provider<EvaluationFeatureConfig> evaluationFeatureConfigProvider,
                                    final LearnerWalkableService learnerWalkableService,
                                    final EvaluationRequestService evaluationRequestService) {
        this.evaluationFeatureConfigProvider = evaluationFeatureConfigProvider;
        this.learnerWalkableService = learnerWalkableService;
        this.evaluationRequestService = evaluationRequestService;
    }

    /**
     * Evaluate an interactive by either invoking the camel implementation or the reactive implementation based on
     * global and account configurations
     *
     * @param deploymentId the deployment id the interactive belongs to
     * @param interactiveId the interactive to evaluate
     * @param producingClientId the client triggering evaluation
     * @param studentId the student triggering evaluation
     * @param name the account shadow attribute name that should map to an evaluation feature configuration value
     * @return a mono containing the evaluation result
     */
    @Trace(async = true)
    public Mono<EvaluationResult> evaluate(final UUID deploymentId, final UUID interactiveId, final String producingClientId,
                                           final UUID studentId, final AccountShadowAttributeName name) {
        // check the name, if that is null use the global config to determine which implementation to use
        final EvaluationFeatureConfig globalConfig = evaluationFeatureConfigProvider.get();
        try {
            EvaluationFeatureConfigurationValues value = Enums.from(name, EvaluationFeatureConfigurationValues.class);

            if (value.equals(EvaluationFeatureConfigurationValues.REACTIVE_EVALUATION)) {
                // return the reactive eval imple
                return reactiveEvaluation(deploymentId, interactiveId, producingClientId, studentId);
            }
            // return the global configuration implementation
            return evaluate(deploymentId, interactiveId, producingClientId, studentId, globalConfig);

        } catch (Throwable throwable) {
            // alright we are definitely falling back to the global config
            return evaluate(deploymentId, interactiveId, producingClientId, studentId, globalConfig);
        }
    }


    /**
     * Evaluate an interactive by either invoking the camel implementation or the reactive implementation based on
     * global and account configurations
     *
     * @param deploymentId the deployment id the interactive belongs to
     * @param interactiveId the interactive to evaluate
     * @param producingClientId the client triggering evaluation
     * @param studentId the student triggering evaluation
     * @param name the account shadow attribute name that should map to an evaluation feature configuration value
     * @param timeId the id for the student scope
     * @return a mono containing the evaluation result
     */
    @Trace(async = true)
    public Mono<EvaluationResult> evaluate(final UUID deploymentId, final UUID interactiveId, final String producingClientId,
                                           final UUID studentId, final AccountShadowAttributeName name, final UUID timeId) {
        // check the name, if that is null use the global config to determine which implementation to use
        final EvaluationFeatureConfig globalConfig = evaluationFeatureConfigProvider.get();
        try {
            EvaluationFeatureConfigurationValues value = Enums.from(name, EvaluationFeatureConfigurationValues.class);

            if (value.equals(EvaluationFeatureConfigurationValues.REACTIVE_EVALUATION)) {
                // return the reactive eval imple
                return reactiveEvaluation(deploymentId, interactiveId, producingClientId, studentId, timeId);
            }
            // return the global configuration implementation
            return evaluate(deploymentId, interactiveId, producingClientId, studentId, globalConfig);

        } catch (Throwable throwable) {
            // alright we are definitely falling back to the global config
            return evaluate(deploymentId, interactiveId, producingClientId, studentId, globalConfig, timeId);
        }
    }

    /**
     * Check whether to fire the camel evaluation or the reactive evaluation based on the global configuration
     *
     * @param deploymentId the deployment id the interactive belongs to
     * @param interactiveId the interactive id to evaluate
     * @param producingClientId the client id that triggered the evaluation
     * @param studentId the student id that triggered evaluation
     * @param globalConfig the evaluation global configuration
     * @return a mono containing the evaluation result
     */
    private Mono<EvaluationResult> evaluate(final UUID deploymentId, final UUID interactiveId, final String producingClientId,
                                            final UUID studentId, EvaluationFeatureConfig globalConfig) {
        if (globalConfig.getConfiguredFeature().equals(EvaluationFeatureConfigurationValues.REACTIVE_EVALUATION)) {
            return reactiveEvaluation(deploymentId, interactiveId, producingClientId, studentId);
        }
        return camelEvaluation(deploymentId, interactiveId, producingClientId, studentId);
    }

    /**
     * Check whether to fire the camel evaluation or the reactive evaluation based on the global configuration
     *
     * @param deploymentId the deployment id the interactive belongs to
     * @param interactiveId the interactive id to evaluate
     * @param producingClientId the client id that triggered the evaluation
     * @param studentId the student id that triggered evaluation
     * @param globalConfig the evaluation global configuration
     * @param timeId the id for the student scope
     * @return a mono containing the evaluation result
     */
    private Mono<EvaluationResult> evaluate(final UUID deploymentId, final UUID interactiveId, final String producingClientId,
                                            final UUID studentId, EvaluationFeatureConfig globalConfig, final UUID timeId) {
        if (globalConfig.getConfiguredFeature().equals(EvaluationFeatureConfigurationValues.REACTIVE_EVALUATION)) {
            return reactiveEvaluation(deploymentId, interactiveId, producingClientId, studentId, timeId);
        }
        return camelEvaluation(deploymentId, interactiveId, producingClientId, studentId);
    }

    /**
     * Invokes the reactive evaluation implementation. Then adapts its response to return an evaluation result
     *
     * @param deploymentId the deployment id the interactive belongs to
     * @param interactiveId the interactive id to evaluate
     * @param producingClientId the client id that triggered the evaluation
     * @param studentId the student id that triggered evaluation
     * @return a mono containing the evaluation result
     */
    @Trace(async = true)
    private Mono<EvaluationResult> reactiveEvaluation(final UUID deploymentId, final UUID interactiveId, final String producingClientId,
                                                      final UUID studentId) {
        // evaluate passing a null parent pathway id (supplying that in the future will spare 1 query)
        return learnerWalkableService.evaluate(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE, studentId, producingClientId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // map the received response to
                .map(response -> {
                    final LearnerEvaluationRequest request = response.getEvaluationRequest();
                    final WalkableEvaluationResult result = response.getWalkableEvaluationResult();

                    return new EvaluationResult()
                            .setAttempt(request.getAttempt())
                            .setAttemptId(request.getAttempt().getId())
                            .setDeployment(request.getDeployment())
                            .setParentId(request.getParentPathwayId())
                            .setId(result.getId())
                            .setCoursewareElementId(request.getLearnerWalkable().getId())
                            .setInteractiveComplete(result.isWalkableComplete())
                            .setScenarioCorrectness(result.getTruthfulScenario().getScenarioCorrectness())
                            .setScenarioEvaluationResults(response.getScenarioEvaluationResults())
                            .setTriggeredActions(result.getTriggeredActions())
                            .setActionResults(result.getActionResults());
                });
    }

    /**
     * Invokes the reactive evaluation implementation. Then adapts its response to return an evaluation result
     *
     * @param deploymentId the deployment id the interactive belongs to
     * @param interactiveId the interactive id to evaluate
     * @param producingClientId the client id that triggered the evaluation
     * @param studentId the student id that triggered evaluation
     * @param timeId the id for the student scope
     * @return a mono containing the evaluation result
     */
    @Trace(async = true)
    private Mono<EvaluationResult> reactiveEvaluation(final UUID deploymentId, final UUID interactiveId, final String producingClientId,
                                                      final UUID studentId, final UUID timeId) {
        // evaluate passing a null parent pathway id (supplying that in the future will spare 1 query)
        return learnerWalkableService.evaluate(deploymentId, interactiveId, CoursewareElementType.INTERACTIVE, studentId, producingClientId, timeId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // map the received response to
                .map(response -> {
                    final LearnerEvaluationRequest request = response.getEvaluationRequest();
                    final WalkableEvaluationResult result = response.getWalkableEvaluationResult();

                    return new EvaluationResult()
                            .setAttempt(request.getAttempt())
                            .setAttemptId(request.getAttempt().getId())
                            .setDeployment(request.getDeployment())
                            .setParentId(request.getParentPathwayId())
                            .setId(result.getId())
                            .setCoursewareElementId(request.getLearnerWalkable().getId())
                            .setInteractiveComplete(result.isWalkableComplete())
                            .setScenarioCorrectness(result.getTruthfulScenario().getScenarioCorrectness())
                            .setScenarioEvaluationResults(response.getScenarioEvaluationResults())
                            .setTriggeredActions(result.getTriggeredActions())
                            .setActionResults(result.getActionResults());
                });
    }

    /**
     * Invokes the camel evaluation implementation
     *
     * @param deploymentId the deployment id the interactive belongs to
     * @param interactiveId the interactive id to evaluate
     * @param producingClientId the client id that triggered the evaluation
     * @param studentId the student id that triggered evaluation
     * @return a mono containing the evaluation result
     */
    private Mono<EvaluationResult> camelEvaluation(final UUID deploymentId, final UUID interactiveId, final String producingClientId,
                                                   final UUID studentId) {
        return evaluationRequestService.evaluate(deploymentId, interactiveId, producingClientId, studentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
