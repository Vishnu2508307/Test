package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScenarioCondition {
    private String conditionType;
    private String conditionStatement;
    private String conditionOperator;
    private List<String> conditionValue;
    private String scenarioOperator;

    @JsonProperty("conditionType")
    public String getConditionType() {
        return conditionType;
    }

    public ScenarioCondition setConditionType(String conditionType) {
        this.conditionType = conditionType;
        return this;
    }

    @JsonProperty("conditionStatement")
    public String getConditionStatement() {
        return conditionStatement;
    }

    public ScenarioCondition setConditionStatement(String conditionStatement) {
        this.conditionStatement = conditionStatement;
        return this;
    }

    @JsonProperty("conditionOperator")
    public String getConditionOperator() {
        return conditionOperator;
    }

    public ScenarioCondition setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
        return this;
    }

    @JsonProperty("conditionValue")
    public List<String> getConditionValue() {
        return conditionValue;
    }

    public ScenarioCondition setConditionValue(List<String> conditionValue) {
        this.conditionValue = conditionValue;
        return this;
    }

    @JsonProperty("scenarioOperator")
    public String getScenarioOperator() {
        return scenarioOperator;
    }

    public ScenarioCondition setScenarioOperator(String scenarioOperator) {
        this.scenarioOperator = scenarioOperator;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioCondition that = (ScenarioCondition) o;
        return Objects.equals(conditionType, that.conditionType) &&
                Objects.equals(conditionStatement, that.conditionStatement) &&
                Objects.equals(conditionOperator, that.conditionOperator) &&
                Objects.equals(conditionValue, that.conditionValue) &&
                Objects.equals(scenarioOperator, that.scenarioOperator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionType, conditionStatement, conditionOperator, conditionValue, scenarioOperator);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
