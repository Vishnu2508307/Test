package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlgorithmInputVariable {
    private String variableName;
    private String possibleValues;

    @JsonProperty("variableName")
    public String getVariableName() {
        return variableName;
    }

    public AlgorithmInputVariable setVariableName(String variableName) {
        this.variableName = variableName;
        return this;
    }

    @JsonProperty("possibleValues")
    public String getPossibleValues() {
        return possibleValues;
    }

    public AlgorithmInputVariable setPossibleValues(String possibleValues) {
        this.possibleValues = possibleValues;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlgorithmInputVariable that = (AlgorithmInputVariable) o;
        return Objects.equals(variableName, that.variableName) &&
                Objects.equals(possibleValues, that.possibleValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableName, possibleValues);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
