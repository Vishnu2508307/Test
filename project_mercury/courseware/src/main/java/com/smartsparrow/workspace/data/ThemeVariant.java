package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.service.ThemeState;

public class ThemeVariant {
    private UUID themeId;
    private UUID variantId;
    private String variantName;
    private String config;
    private ThemeState state;

    public UUID getThemeId() {
        return themeId;
    }

    public ThemeVariant setThemeId(final UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public ThemeVariant setVariantId(final UUID variantId) {
        this.variantId = variantId;
        return this;
    }

    public String getVariantName() {
        return variantName;
    }

    public ThemeVariant setVariantName(final String variantName) {
        this.variantName = variantName;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public ThemeVariant setConfig(final String config) {
        this.config = config;
        return this;
    }

    public ThemeState getState() {
        return state;
    }

    public ThemeVariant setState(final ThemeState state) {
        this.state = state;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeVariant that = (ThemeVariant) o;
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
        return "ThemeVariant{" +
                "themeId=" + themeId +
                ", variantId=" + variantId +
                ", variantName='" + variantName + '\'' +
                ", config='" + config + '\'' +
                ", state=" + state +
                '}';
    }
}
