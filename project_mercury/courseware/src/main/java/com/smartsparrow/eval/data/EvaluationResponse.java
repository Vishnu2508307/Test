package com.smartsparrow.eval.data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the result returned by an Evaluation
 */
public interface EvaluationResponse<T extends EvaluationRequest> {

    /**
     * @return the evaluation request that generated this response
     */
    T getEvaluationRequest();

    /**
     * @return the list of produced scenario evaluation results
     */
    List<ScenarioEvaluationResult> getScenarioEvaluationResults();

    /**
     * Find a scenarioEvaluationResult in the list that evaluated to <code>true</code>
     *
     * @param scenarioEvaluationResults the list of scenario evaluation results to extract the truthful result from
     * @return the scenario that evaluated to <code>true</code> or an empty scenario when all of them evaluated to
     * <code>false</code> or the list was empty
     */
    default ScenarioEvaluationResult findTruthful(List<ScenarioEvaluationResult> scenarioEvaluationResults) {
        return scenarioEvaluationResults.stream()
                .filter(res -> Boolean.TRUE.equals(res.getEvaluationResult()))
                .findFirst()
                .orElse(new ScenarioEvaluationResult());
    }

    /**
     * Maps a list of scenario evaluation results to a list of scenario ids
     *
     * @param scenarioEvaluationResults the list of scenario evaluation results to map
     * @return a list containing the scenario ids that where triggered
     */
    default List<UUID> getTriggeredScenarioIds(final List<ScenarioEvaluationResult> scenarioEvaluationResults) {
        return scenarioEvaluationResults.stream()
                .map(ScenarioEvaluationResult::getScenarioId)
                .collect(Collectors.toList());
    }
}
