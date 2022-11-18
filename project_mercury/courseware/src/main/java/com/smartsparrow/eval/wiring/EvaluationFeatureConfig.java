package com.smartsparrow.eval.wiring;

import java.util.Objects;

public class EvaluationFeatureConfig {

    private EvaluationFeatureConfigurationValues configuredFeature;

    public EvaluationFeatureConfigurationValues getConfiguredFeature() {
        return configuredFeature;
    }

    public EvaluationFeatureConfig setConfiguredFeature(EvaluationFeatureConfigurationValues configuredFeature) {
        this.configuredFeature = configuredFeature;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationFeatureConfig that = (EvaluationFeatureConfig) o;
        return configuredFeature == that.configuredFeature;
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuredFeature);
    }

    @Override
    public String toString() {
        return "EvaluationFeatureConfig{" +
                "configuredFeature=" + configuredFeature +
                '}';
    }
}
