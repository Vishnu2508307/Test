package com.smartsparrow.eval.data;

import java.util.List;
import java.util.Objects;

public class TestEvaluationResponse implements EvaluationResponse<TestEvaluationRequest> {


    private TestEvaluationRequest evaluationRequest;
    private List<ScenarioEvaluationResult> scenarioEvaluationResults;

    @Override
    public TestEvaluationRequest getEvaluationRequest() {
        return evaluationRequest;
    }

    @Override
    public List<ScenarioEvaluationResult> getScenarioEvaluationResults() {
        return scenarioEvaluationResults;
    }

    public TestEvaluationResponse setEvaluationRequest(TestEvaluationRequest evaluationRequest) {
        this.evaluationRequest = evaluationRequest;
        return this;
    }

    public TestEvaluationResponse setScenarioEvaluationResults(List<ScenarioEvaluationResult> scenarioEvaluationResults) {
        this.scenarioEvaluationResults = scenarioEvaluationResults;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEvaluationResponse that = (TestEvaluationResponse) o;
        return Objects.equals(evaluationRequest, that.evaluationRequest) && Objects.equals(scenarioEvaluationResults, that.scenarioEvaluationResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationRequest, scenarioEvaluationResults);
    }

    @Override
    public String toString() {
        return "TestEvaluationResponse{" +
                "evaluationRequest=" + evaluationRequest +
                ", scenarioEvaluationResults=" + scenarioEvaluationResults +
                '}';
    }
}
