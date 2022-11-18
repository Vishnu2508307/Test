package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemAlgorithmicAnswerInputs {
    private String algorithmUrl;
    private String algorithmId;
    private List<AlgorithmInputVariable> algorithmInputVariable;

    @JsonProperty("algorithmUrl")
    public String getAlgorithmUrl() {
        return algorithmUrl;
    }

    public ItemAlgorithmicAnswerInputs setAlgorithmUrl(String algorithmUrl) {
        this.algorithmUrl = algorithmUrl;
        return this;
    }

    @JsonProperty("algorithmId")
    public String getAlgorithmId() {
        return algorithmId;
    }

    public ItemAlgorithmicAnswerInputs setAlgorithmId(String algorithmId) {
        this.algorithmId = algorithmId;
        return this;
    }

    @JsonProperty("algorithmInputVariable")
    public List<AlgorithmInputVariable> getAlgorithmInputVariable() {
        return algorithmInputVariable;
    }

    public ItemAlgorithmicAnswerInputs setAlgorithmInputVariable(List<AlgorithmInputVariable> algorithmInputVariable) {
        this.algorithmInputVariable = algorithmInputVariable;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemAlgorithmicAnswerInputs that = (ItemAlgorithmicAnswerInputs) o;
        return Objects.equals(algorithmUrl, that.algorithmUrl) &&
                Objects.equals(algorithmId, that.algorithmId) &&
                Objects.equals(algorithmInputVariable, that.algorithmInputVariable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithmUrl, algorithmId, algorithmInputVariable);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
