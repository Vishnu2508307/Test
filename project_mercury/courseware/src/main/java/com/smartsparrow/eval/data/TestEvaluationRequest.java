package com.smartsparrow.eval.data;

import java.util.Objects;

import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.data.Walkable;

public class TestEvaluationRequest implements EvaluationRequest {

    private Walkable walkable;
    private String data;
    private ScenarioLifecycle scenarioLifecycle;

    @Override
    public Type getType() {
        return Type.TEST;
    }

    public Walkable getWalkable() {
        return walkable;
    }

    public TestEvaluationRequest setWalkable(Walkable walkable) {
        this.walkable = walkable;
        return this;
    }

    public String getData() {
        return data;
    }

    public TestEvaluationRequest setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public ScenarioLifecycle getScenarioLifecycle() {
        return scenarioLifecycle;
    }

    public TestEvaluationRequest setScenarioLifecycle(ScenarioLifecycle scenarioLifecycle) {
        this.scenarioLifecycle = scenarioLifecycle;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEvaluationRequest that = (TestEvaluationRequest) o;
        return Objects.equals(walkable, that.walkable) &&
                Objects.equals(data, that.data) &&
                Objects.equals(scenarioLifecycle, that.scenarioLifecycle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walkable, data, scenarioLifecycle);
    }

    @Override
    public String toString() {
        return "TestEvaluationRequest{" +
                "walkable=" + walkable +
                ", data='" + data + '\'' +
                ", scenarioLifecycle='" + scenarioLifecycle + '\'' +
                '}';
    }
}
