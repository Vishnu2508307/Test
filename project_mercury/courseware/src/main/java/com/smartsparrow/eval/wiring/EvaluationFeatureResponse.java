package com.smartsparrow.eval.wiring;

import java.util.Objects;

public class EvaluationFeatureResponse {

    private EvaluationFeatureConfigurationValues configuredFeature;

    public EvaluationFeatureConfigurationValues getConfiguredFeature() {
        return configuredFeature;
    }

    public EvaluationFeatureResponse setConfiguredFeature(EvaluationFeatureConfigurationValues configuredFeature) {
        this.configuredFeature = configuredFeature;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationFeatureResponse that = (EvaluationFeatureResponse) o;
        return configuredFeature == that.configuredFeature;
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuredFeature);
    }

    @Override
    public String toString() {
        return "EvaluationFeatureResponse{" +
                "configuredFeature=" + configuredFeature +
                '}';
    }
}
