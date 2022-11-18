package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartScenario {
    private List<ScenarioCondition> scenarioConditions;
    private List<ResultingAction> resultingActions;

    @JsonProperty("scenarioConditions")
    public List<ScenarioCondition> getScenarioConditions() {
        return scenarioConditions;
    }

    public ItemPartScenario setScenarioConditions(List<ScenarioCondition> scenarioConditions) {
        this.scenarioConditions = scenarioConditions;
        return this;
    }

    @JsonProperty("resultingActions")
    public List<ResultingAction> getResultingActions() {
        return resultingActions;
    }

    public ItemPartScenario setResultingActions(List<ResultingAction> resultingActions) {
        this.resultingActions = resultingActions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPartScenario that = (ItemPartScenario) o;
        return Objects.equals(scenarioConditions, that.scenarioConditions) &&
                Objects.equals(resultingActions, that.resultingActions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioConditions, resultingActions);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}

