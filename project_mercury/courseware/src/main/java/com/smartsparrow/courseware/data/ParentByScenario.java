package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class ParentByScenario {

    private UUID parentId;
    private CoursewareElementType parentType;
    private UUID scenarioId;

    public UUID getParentId() {
        return parentId;
    }

    public ParentByScenario setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    public ParentByScenario setParentType(CoursewareElementType parentType) {
        this.parentType = parentType;
        return this;
    }

    public UUID getScenarioId() {
        return scenarioId;
    }

    public ParentByScenario setScenarioId(UUID scenarioId) {
        this.scenarioId = scenarioId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentByScenario that = (ParentByScenario) o;
        return Objects.equals(parentId, that.parentId) &&
                parentType == that.parentType &&
                Objects.equals(scenarioId, that.scenarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, parentType, scenarioId);
    }

    @Override
    public String toString() {
        return "ParentByScenario{" +
                "parentId=" + parentId +
                ", parentType=" + parentType +
                ", scenarioId=" + scenarioId +
                '}';
    }
}
