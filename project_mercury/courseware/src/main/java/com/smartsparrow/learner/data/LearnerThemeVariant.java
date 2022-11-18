package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.service.ThemeState;

public class LearnerThemeVariant {
    private UUID themeId;
    private UUID variantId;
    private String variantName;
    private String config;
    private ThemeState state;

    public UUID getThemeId() {
        return themeId;
    }

    public LearnerThemeVariant setThemeId(final UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public LearnerThemeVariant setVariantId(final UUID variantId) {
        this.variantId = variantId;
        return this;
    }

    public String getVariantName() {
        return variantName;
    }

    public LearnerThemeVariant setVariantName(final String variantName) {
        this.variantName = variantName;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public LearnerThemeVariant setConfig(final String config) {
        this.config = config;
        return this;
    }

    public ThemeState getState() {
        return state;
    }

    public LearnerThemeVariant setState(final ThemeState state) {
        this.state = state;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerThemeVariant that = (LearnerThemeVariant) o;
        return Objects.equals(themeId, that.themeId) &&
                Objects.equals(variantId, that.variantId) &&
                Objects.equals(variantName, that.variantName) &&
                Objects.equals(config, that.config)&&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, variantId, variantName, config, state);
    }

    @Override
    public String toString() {
        return "LearnerThemeVariant{" +
                "themeId=" + themeId +
                ", variantId=" + variantId +
                ", variantName='" + variantName + '\'' +
                ", config='" + config + '\'' +
                ", state=" + state +
                '}';
    }
}
