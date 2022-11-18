package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class Scenario {

    private UUID id;
    private String condition;
    private String actions;
    private ScenarioLifecycle lifecycle;
    private String name;
    private String description;
    private ScenarioCorrectness correctness;

    public UUID getId() {
        return id;
    }

    public Scenario setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getCondition() {
        return condition;
    }

    public Scenario setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public String getActions() {
        return actions;
    }

    public Scenario setActions(String action) {
        this.actions = action;
        return this;
    }

    public ScenarioLifecycle getLifecycle() {
        return lifecycle;
    }

    public Scenario setLifecycle(ScenarioLifecycle scenarioLifecycle) {
        this.lifecycle = scenarioLifecycle;
        return this;
    }

    public String getName() {
        return name;
    }

    public Scenario setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Scenario setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the correctness
     *
     * @return the scenario correctness or <code>null</code> when correctness not defined
     */
    public ScenarioCorrectness getCorrectness() {
        return correctness;
    }

    public Scenario setCorrectness(ScenarioCorrectness correctness) {
        this.correctness = correctness;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scenario scenario = (Scenario) o;
        return Objects.equals(id, scenario.id) &&
                Objects.equals(condition, scenario.condition) &&
                Objects.equals(actions, scenario.actions) &&
                lifecycle == scenario.lifecycle &&
                Objects.equals(name, scenario.name) &&
                Objects.equals(description, scenario.description) &&
                Objects.equals(correctness, scenario.correctness);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, condition, actions, lifecycle, name, description, correctness);
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "id=" + id +
                ", condition='" + condition + '\'' +
                ", actions='" + actions + '\'' +
                ", lifecycle=" + lifecycle +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", correctness='" + correctness + '\'' +
                '}';
    }
}
