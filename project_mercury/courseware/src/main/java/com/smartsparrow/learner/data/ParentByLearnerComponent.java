package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ParentByComponent;

public class ParentByLearnerComponent extends ParentByComponent {

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public ParentByLearnerComponent setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public ParentByLearnerComponent setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public ParentByLearnerComponent setComponentId(UUID componentId) {
        super.setComponentId(componentId);
        return this;
    }

    @Override
    public ParentByLearnerComponent setParentId(UUID parentId) {
        super.setParentId(parentId);
        return this;
    }

    @Override
    public ParentByLearnerComponent setParentType(CoursewareElementType parentType) {
        super.setParentType(parentType);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParentByLearnerComponent that = (ParentByLearnerComponent) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "ParentByLearnerComponent{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                "} " + super.toString();
    }
}
