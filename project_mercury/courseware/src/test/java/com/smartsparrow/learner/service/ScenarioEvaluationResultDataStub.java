package com.smartsparrow.learner.service;

import java.util.UUID;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;

public class ScenarioEvaluationResultDataStub {

    static UUID scenarioId = UUIDs.timeBased();
    static boolean scenarioEvaluationResult = true;
    static ScenarioCorrectness scenarioCorrectness = ScenarioCorrectness.correct;
    //language=JSON
    static private String actionsJson = "[\n"
            + ActionDataStub.progressActionInteractiveCompleteJson() + ","
            + ActionDataStub.feedbackActionLiteralJson()
            + "]";

    public static ScenarioEvaluationResult scenarioEvaluationResult() {
        return scenarioEvaluationResult(scenarioId, scenarioEvaluationResult, scenarioCorrectness, actionsJson);
    }

    public static ScenarioEvaluationResult scenarioEvaluationResult(boolean scenarioEvaluationResult) {
        return scenarioEvaluationResult(scenarioId, scenarioEvaluationResult, scenarioCorrectness, actionsJson);
    }

    public static ScenarioEvaluationResult scenarioEvaluationResult(UUID scenarioId,
            boolean evaluationResult,
            ScenarioCorrectness scenarioCorrectness,
            String actionsJson) {
        return new ScenarioEvaluationResult()
                .setScenarioId(scenarioId)
                .setScenarioCorrectness(scenarioCorrectness)
                .setEvaluationResult(evaluationResult)
                .setActions(actionsJson);
    }

}
