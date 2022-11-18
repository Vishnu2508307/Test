package com.smartsparrow.learner.data;

import java.util.Objects;

public class ThemeConfig {
    String config;

    public String getConfig() {
        return config;
    }

    public ThemeConfig setConfig(final String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeConfig that = (ThemeConfig) o;
        return Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config);
    }

    @Override
    public String toString() {
        return "ThemeConfig{" +
                "config='" + config + '\'' +
                '}';
    }
}
