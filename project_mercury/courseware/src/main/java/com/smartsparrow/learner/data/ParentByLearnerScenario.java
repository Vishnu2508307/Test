package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ParentByScenario;

public class ParentByLearnerScenario extends ParentByScenario {

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public ParentByLearnerScenario setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public ParentByLearnerScenario setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public ParentByLearnerScenario setParentId(UUID parentId) {
        super.setParentId(parentId);
        return this;
    }

    @Override
    public ParentByLearnerScenario setParentType(CoursewareElementType parentType) {
        super.setParentType(parentType);
        return this;
    }

    @Override
    public ParentByLearnerScenario setScenarioId(UUID scenarioId) {
        super.setScenarioId(scenarioId);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParentByLearnerScenario that = (ParentByLearnerScenario) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "ParentByLearnerScenario{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                "} " + super.toString();
    }
}
