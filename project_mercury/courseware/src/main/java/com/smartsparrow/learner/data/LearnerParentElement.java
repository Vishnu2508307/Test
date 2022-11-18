package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

public class LearnerParentElement {

    private UUID elementId;
    private UUID deploymentId;
    private UUID changeId;
    private UUID parentId;

    public UUID getElementId() {
        return elementId;
    }

    public LearnerParentElement setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerParentElement setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerParentElement setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getParentId() {
        return parentId;
    }

    public LearnerParentElement setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerParentElement that = (LearnerParentElement) o;
        return Objects.equals(elementId, that.elementId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(parentId, that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, deploymentId, changeId, parentId);
    }

    @Override
    public String toString() {
        return "LearnerParentElement{" +
                "elementId=" + elementId +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", parentId=" + parentId +
                '}';
    }
}
