package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class ManualGradingConfiguration {

    private UUID componentId;
    private Double maxScore;

    public UUID getComponentId() {
        return componentId;
    }

    public ManualGradingConfiguration setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public ManualGradingConfiguration setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualGradingConfiguration that = (ManualGradingConfiguration) o;
        return Objects.equals(componentId, that.componentId) &&
                Objects.equals(maxScore, that.maxScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, maxScore);
    }

    @Override
    public String toString() {
        return "ManualGradingConfiguration{" +
                "componentId=" + componentId +
                ", maxScore=" + maxScore +
                '}';
    }
}
