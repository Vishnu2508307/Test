package com.smartsparrow.eval.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EvaluationFeatureMode {
    @JsonProperty("processingMode")
    private String processingMode;

    public String getProcessingMode() {
        return processingMode;
    }

    public EvaluationFeatureMode setProcessingMode(final String processingMode) {
        this.processingMode = processingMode;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationFeatureMode that = (EvaluationFeatureMode) o;
        return Objects.equals(processingMode, that.processingMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processingMode);
    }

    @Override
    public String toString() {
        return "EvaluationFeatureMode{" +
                "processingMode='" + processingMode + '\'' +
                '}';
    }
}
