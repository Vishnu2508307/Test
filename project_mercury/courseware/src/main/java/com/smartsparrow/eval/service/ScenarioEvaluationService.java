package com.smartsparrow.eval.service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.eval.condition.ConditionEvaluator;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.deserializer.ConditionDeserializer;
import com.smartsparrow.eval.lang.ScenarioEvaluationException;
import com.smartsparrow.eval.lang.UnableToResolveException;
import com.smartsparrow.eval.resolver.ConditionResolver;
import com.smartsparrow.learner.data.EvaluationContext;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class ScenarioEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioEvaluationService.class);

    private final ConditionDeserializer conditionDeserializer;
    private final ConditionResolver conditionResolver;
    private final ConditionEvaluator conditionEvaluator;

    @Inject
    public ScenarioEvaluationService(ConditionDeserializer conditionDeserializer,
                                     ConditionResolver conditionResolver,
                                     ConditionEvaluator conditionEvaluator) {
        this.conditionDeserializer = conditionDeserializer;
        this.conditionResolver = conditionResolver;
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Deserialize, resolve and evaluate a condition in a reactive way.
     *
     * @param scenario the scenario to evaluate
     * @param evaluationContext the evaluation context to evaluate the scenario for
     * @return a mono of scenario evaluation result
     * @throws ScenarioEvaluationException when failing to evaluate a scenario
     */
    @Trace(async = true)
    public Mono<ScenarioEvaluationResult> evaluateCondition(Scenario scenario, EvaluationContext evaluationContext) {
        return conditionDeserializer.deserialize(scenario.getCondition())
                .flatMap(deserializedCondition -> {
                    if (deserializedCondition.getConditions().isEmpty()) {
                        return buildScenarioEvaluationResult(scenario, true, null);
                    }
                    return conditionResolver.resolve(deserializedCondition, evaluationContext)
                            .map(resolvedCondition -> new ScenarioEvaluationResult()
                                    .setScenarioId(scenario.getId())
                                    .setEvaluationResult(conditionEvaluator.evaluate(resolvedCondition))
                                    .setScenarioCorrectness(scenario.getCorrectness())
                                    .setActions(scenario.getActions()))
                            .onErrorResume(error -> {
                                if (error instanceof UnableToResolveException || error instanceof UnsupportedOperationException) {
                                    return buildScenarioEvaluationResult(scenario, false, error.getMessage());
                                } else {
                                    return Mono.error(error);
                                }
                            });
                })
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("error while evaluating scenario %s", scenario.getId()), throwable);
                    }
                    throw new ScenarioEvaluationException(scenario.getId(), throwable);
                });
    }

    /**
     * Build a scenario evaluation result
     *
     * @param scenario  the scenario to build the evaluation result for
     * @param evaluationResult the boolean value representing the evaluation result of the scenario
     * @return a mono of scenario evaluation result
     */
    @Trace(async = true)
    private Mono<ScenarioEvaluationResult> buildScenarioEvaluationResult(final Scenario scenario,
                                                                         final boolean evaluationResult,
                                                                         @Nullable final String errorMessage) {
        return Mono.just(new ScenarioEvaluationResult()
                .setEvaluationResult(evaluationResult)
                .setScenarioId(scenario.getId())
                .setActions(scenario.getActions())
                .setScenarioCorrectness(scenario.getCorrectness())
                .setErrorMessage(errorMessage));
    }
}
