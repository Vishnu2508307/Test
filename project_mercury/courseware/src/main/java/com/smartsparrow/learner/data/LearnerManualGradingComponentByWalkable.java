package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class LearnerManualGradingComponentByWalkable {

    private UUID deploymentId;
    private UUID walkableId;
    private UUID componentId;
    private UUID changeId;
    private CoursewareElementType walkableType;
    private UUID componentParentId;
    private CoursewareElementType componentParentType;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerManualGradingComponentByWalkable setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getWalkableId() {
        return walkableId;
    }

    public LearnerManualGradingComponentByWalkable setWalkableId(UUID walkableId) {
        this.walkableId = walkableId;
        return this;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public LearnerManualGradingComponentByWalkable setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerManualGradingComponentByWalkable setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public CoursewareElementType getWalkableType() {
        return walkableType;
    }

    public LearnerManualGradingComponentByWalkable setWalkableType(CoursewareElementType walkableType) {
        this.walkableType = walkableType;
        return this;
    }

    public UUID getComponentParentId() {
        return componentParentId;
    }

    public LearnerManualGradingComponentByWalkable setComponentParentId(UUID componentParentId) {
        this.componentParentId = componentParentId;
        return this;
    }

    public CoursewareElementType getComponentParentType() {
        return componentParentType;
    }

    public LearnerManualGradingComponentByWalkable setComponentParentType(CoursewareElementType componentParentType) {
        this.componentParentType = componentParentType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerManualGradingComponentByWalkable that = (LearnerManualGradingComponentByWalkable) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(walkableId, that.walkableId) &&
                Objects.equals(componentId, that.componentId) &&
                Objects.equals(changeId, that.changeId) &&
                walkableType == that.walkableType &&
                Objects.equals(componentParentId, that.componentParentId) &&
                componentParentType == that.componentParentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, walkableId, componentId, changeId, walkableType, componentParentId,
                componentParentType);
    }

    @Override
    public String toString() {
        return "LearnerManualGradingComponentByWalkable{" +
                "deploymentId=" + deploymentId +
                ", walkableId=" + walkableId +
                ", componentId=" + componentId +
                ", changeId=" + changeId +
                ", walkableType=" + walkableType +
                ", componentParentId=" + componentParentId +
                ", componentParentType=" + componentParentType +
                '}';
    }
}
