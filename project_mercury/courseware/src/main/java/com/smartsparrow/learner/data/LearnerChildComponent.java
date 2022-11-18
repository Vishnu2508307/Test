package com.smartsparrow.learner.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LearnerChildComponent {

    private UUID parentId;
    private UUID deploymentId;
    private UUID changeId;
    private List<UUID> componentIds;

    public UUID getParentId() {
        return parentId;
    }

    public LearnerChildComponent setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerChildComponent setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerChildComponent setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public List<UUID> getComponentIds() {
        return componentIds;
    }

    public LearnerChildComponent setComponentIds(List<UUID> componentIds) {
        this.componentIds = componentIds;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerChildComponent that = (LearnerChildComponent) o;
        return Objects.equals(parentId, that.parentId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(componentIds, that.componentIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, deploymentId, changeId, componentIds);
    }

    @Override
    public String toString() {
        return "LearnerChildComponent{" +
                "parentId=" + parentId +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", componentIds=" + componentIds +
                '}';
    }
}
