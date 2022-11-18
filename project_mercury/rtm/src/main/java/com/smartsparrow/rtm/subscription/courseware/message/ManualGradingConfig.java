package com.smartsparrow.rtm.subscription.courseware.message;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.courseware.data.ManualGradingConfiguration;

/**
 * This Serializable class was created to fix a spotBugsMain error: Non-transient non-serializable instance field in serializable class
 * The above error was thrown when using {@link ManualGradingConfiguration} as an instance field in {@link ComponentManualGradingBroadcastMessage}
 */
public class ManualGradingConfig implements Serializable {

    private static final long serialVersionUID = -8245692877871046917L;

    private UUID componentId;
    private Double maxScore;

    public UUID getComponentId() {
        return componentId;
    }

    public ManualGradingConfig setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public ManualGradingConfig setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
        return this;
    }

    public static ManualGradingConfig from(@Nonnull ManualGradingConfiguration manualGradingConfiguration) {

        return new ManualGradingConfig()
                .setComponentId(manualGradingConfiguration.getComponentId())
                .setMaxScore(manualGradingConfiguration.getMaxScore());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualGradingConfig that = (ManualGradingConfig) o;
        return Objects.equals(componentId, that.componentId) &&
                Objects.equals(maxScore, that.maxScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, maxScore);
    }

    @Override
    public String toString() {
        return "ManualGradingConfig{" +
                "componentId=" + componentId +
                ", maxScore=" + maxScore +
                '}';
    }
}
