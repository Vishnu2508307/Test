package com.smartsparrow.courseware.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;

public class ScenarioPayload {

    private UUID parentId;
    private CoursewareElementType parentType;
    private UUID scenarioId;
    private String description;
    private String condition;
    private String actions;
    private ScenarioLifecycle lifecycle;
    private String name;
    private ScenarioCorrectness correctness;

    public static ScenarioPayload from(@Nonnull UUID parentId,
                                       @Nonnull CoursewareElementType parentType,
                                       @Nonnull Scenario scenario) {
        ScenarioPayload payload = new ScenarioPayload();
        payload.setParentId(parentId);
        payload.setParentType(parentType);
        payload.scenarioId = scenario.getId();
        payload.description = scenario.getDescription();
        payload.condition = scenario.getCondition();
        payload.actions = scenario.getActions();
        payload.lifecycle = scenario.getLifecycle();
        payload.name = scenario.getName();
        payload.correctness = scenario.getCorrectness();
        return payload;
    }

    public UUID getParentId() {
        return parentId;
    }

    public ScenarioPayload setParentId(final UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    public ScenarioPayload setParentType(final CoursewareElementType parentType) {
        this.parentType = parentType;
        return this;
    }

    public UUID getScenarioId() {
        return scenarioId;
    }

    public String getDescription() {
        return description;
    }

    public String getCondition() {
        return condition;
    }

    public String getActions() {
        return actions;
    }

    public ScenarioLifecycle getLifecycle() {
        return lifecycle;
    }

    public String getName() {
        return name;
    }

    public ScenarioCorrectness getCorrectness() {
        return correctness;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioPayload that = (ScenarioPayload) o;
        return Objects.equals(parentId, that.parentId) &&
                parentType == that.parentType &&
                Objects.equals(scenarioId, that.scenarioId) &&
                Objects.equals(description, that.description) &&
                Objects.equals(condition, that.condition) &&
                Objects.equals(actions, that.actions) &&
                lifecycle == that.lifecycle &&
                Objects.equals(name, that.name) &&
                correctness == that.correctness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, parentType, scenarioId, description, condition, actions, lifecycle, name, correctness);
    }

    @Override
    public String toString() {
        return "ScenarioPayload{" +
                "rootElementId=" + parentId +
                ", parentType=" + parentType +
                ", scenarioId=" + scenarioId +
                ", description='" + description + '\'' +
                ", condition='" + condition + '\'' +
                ", actions='" + actions + '\'' +
                ", lifecycle=" + lifecycle +
                ", name='" + name + '\'' +
                ", correctness=" + correctness +
                '}';
    }
}
