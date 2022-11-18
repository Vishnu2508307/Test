package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;

public class LearnerScenario extends Scenario {

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerScenario setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerScenario setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public LearnerScenario setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public LearnerScenario setCondition(String condition) {
        super.setCondition(condition);
        return this;
    }

    @Override
    public LearnerScenario setActions(String action) {
        super.setActions(action);
        return this;
    }

    @Override
    public LearnerScenario setLifecycle(ScenarioLifecycle scenarioLifecycle) {
        super.setLifecycle(scenarioLifecycle);
        return this;
    }

    @Override
    public LearnerScenario setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public LearnerScenario setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    @Override
    public LearnerScenario setCorrectness(ScenarioCorrectness correctness) {
        super.setCorrectness(correctness);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerScenario that = (LearnerScenario) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "LearnerScenario{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                "} " + super.toString();
    }
}
