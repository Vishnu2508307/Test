package com.smartsparrow.eval.data;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.Objects;

public class LearnerEvaluationResponse implements EvaluationResponse<LearnerEvaluationRequest> {

    private LearnerEvaluationRequest evaluationRequest;
    private List<ScenarioEvaluationResult> scenarioEvaluationResults;
    private WalkableEvaluationResult walkableEvaluationResult;

    @Override
    public LearnerEvaluationRequest getEvaluationRequest() {
        return evaluationRequest;
    }

    @Override
    public List<ScenarioEvaluationResult> getScenarioEvaluationResults() {
        return scenarioEvaluationResults;
    }

    public LearnerEvaluationResponse setEvaluationRequest(LearnerEvaluationRequest evaluationRequest) {
        this.evaluationRequest = evaluationRequest;
        return this;
    }

    public LearnerEvaluationResponse setScenarioEvaluationResults(List<ScenarioEvaluationResult> scenarioEvaluationResults) {
        this.scenarioEvaluationResults = scenarioEvaluationResults;
        return this;
    }

    public WalkableEvaluationResult getWalkableEvaluationResult() {
        return walkableEvaluationResult;
    }

    public LearnerEvaluationResponse setWalkableEvaluationResult(WalkableEvaluationResult walkableEvaluationResult) {
        this.walkableEvaluationResult = walkableEvaluationResult;
        return this;
    }

    public LearnerEvaluationResponse setScenarioCorrectness() {
        affirmArgument(this.walkableEvaluationResult != null, "walkableEvaluationResult must not be null");
        affirmArgument(this.scenarioEvaluationResults != null, "scenarioEvaluationResults must not be null");
        this.walkableEvaluationResult.setTruthfulScenario(findTruthful(this.scenarioEvaluationResults));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerEvaluationResponse that = (LearnerEvaluationResponse) o;
        return Objects.equals(evaluationRequest, that.evaluationRequest) &&
                Objects.equals(scenarioEvaluationResults, that.scenarioEvaluationResults) &&
                Objects.equals(walkableEvaluationResult, that.walkableEvaluationResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationRequest, scenarioEvaluationResults, walkableEvaluationResult);
    }

    @Override
    public String toString() {
        return "LearnerEvaluationResponse{" +
                "evaluationRequest=" + evaluationRequest +
                ", scenarioEvaluationResults=" + scenarioEvaluationResults +
                ", walkableEvaluationResult=" + walkableEvaluationResult +
                '}';
    }
}
