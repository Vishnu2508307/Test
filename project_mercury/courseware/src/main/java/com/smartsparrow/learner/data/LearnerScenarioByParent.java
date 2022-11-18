package com.smartsparrow.learner.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioByParent;
import com.smartsparrow.courseware.data.ScenarioLifecycle;

public class LearnerScenarioByParent extends ScenarioByParent {

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerScenarioByParent setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerScenarioByParent setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public LearnerScenarioByParent setParentId(UUID parentId) {
        super.setParentId(parentId);
        return this;
    }

    @Override
    public LearnerScenarioByParent setLifecycle(ScenarioLifecycle lifecycle) {
        super.setLifecycle(lifecycle);
        return this;
    }

    @Override
    public LearnerScenarioByParent setScenarioIds(List<UUID> scenarioIds) {
        super.setScenarioIds(scenarioIds);
        return this;
    }

    @Override
    public LearnerScenarioByParent setParentType(CoursewareElementType parentType) {
        super.setParentType(parentType);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerScenarioByParent that = (LearnerScenarioByParent) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "LearnerScenarioByParent{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                "} " + super.toString();
    }
}
