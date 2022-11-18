package com.smartsparrow.courseware.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ScenarioByParent {

    private UUID parentId;
    private ScenarioLifecycle lifecycle;
    private List<UUID> scenarioIds;
    @JsonIgnore
    private CoursewareElementType parentType;

    public UUID getParentId() {
        return parentId;
    }

    public ScenarioByParent setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public ScenarioLifecycle getLifecycle() {
        return lifecycle;
    }

    public ScenarioByParent setLifecycle(ScenarioLifecycle lifecycle) {
        this.lifecycle = lifecycle;
        return this;
    }

    public List<UUID> getScenarioIds() {
        return Collections.unmodifiableList(scenarioIds);
    }

    public ScenarioByParent setScenarioIds(List<UUID> scenarioIds) {
        this.scenarioIds = new ArrayList<>(scenarioIds);
        return this;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    public ScenarioByParent setParentType(CoursewareElementType parentType) {
        this.parentType = parentType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioByParent that = (ScenarioByParent) o;
        return Objects.equals(parentId, that.parentId) &&
                lifecycle == that.lifecycle &&
                Objects.equals(scenarioIds, that.scenarioIds) &&
                parentType == that.parentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, lifecycle, scenarioIds, parentType);
    }

    @Override
    public String toString() {
        return "ScenarioByParent{" +
                "parentId=" + parentId +
                ", lifecycle=" + lifecycle +
                ", scenarioIds=" + scenarioIds +
                ", parentType=" + parentType +
                '}';
    }
}
