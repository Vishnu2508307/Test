package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class ManualGradingComponentByWalkable {

    private UUID walkableId;
    private UUID componentId;
    private CoursewareElementType walkableType;
    private UUID componentParentId;
    private CoursewareElementType parentComponentType;

    public UUID getWalkableId() {
        return walkableId;
    }

    public ManualGradingComponentByWalkable setWalkableId(UUID walkableId) {
        this.walkableId = walkableId;
        return this;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public ManualGradingComponentByWalkable setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    public CoursewareElementType getWalkableType() {
        return walkableType;
    }

    public ManualGradingComponentByWalkable setWalkableType(CoursewareElementType walkableType) {
        this.walkableType = walkableType;
        return this;
    }

    public UUID getComponentParentId() {
        return componentParentId;
    }

    public ManualGradingComponentByWalkable setComponentParentId(UUID componentParentId) {
        this.componentParentId = componentParentId;
        return this;
    }

    public CoursewareElementType getParentComponentType() {
        return parentComponentType;
    }

    public ManualGradingComponentByWalkable setParentComponentType(CoursewareElementType parentComponentType) {
        this.parentComponentType = parentComponentType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualGradingComponentByWalkable that = (ManualGradingComponentByWalkable) o;
        return Objects.equals(walkableId, that.walkableId) &&
                Objects.equals(componentId, that.componentId) &&
                walkableType == that.walkableType &&
                Objects.equals(componentParentId, that.componentParentId) &&
                parentComponentType == that.parentComponentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(walkableId, componentId, walkableType, componentParentId, parentComponentType);
    }

    @Override
    public String toString() {
        return "ManualGradingComponentByWalkable{" +
                "walkableId=" + walkableId +
                ", componentId=" + componentId +
                ", walkableType=" + walkableType +
                ", componentParentId=" + componentParentId +
                ", parentComponentType=" + parentComponentType +
                '}';
    }
}
