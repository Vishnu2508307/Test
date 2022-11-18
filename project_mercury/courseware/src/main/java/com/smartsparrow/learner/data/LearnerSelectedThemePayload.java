package com.smartsparrow.learner.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class LearnerSelectedThemePayload {
    private UUID elementId;
    private UUID themeId;
    private String themeName;
    private List<LearnerThemeVariant> themeVariants;

    public UUID getElementId() {
        return elementId;
    }

    public LearnerSelectedThemePayload setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getThemeId() {
        return themeId;
    }

    public LearnerSelectedThemePayload setThemeId(final UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public String getThemeName() {
        return themeName;
    }

    public LearnerSelectedThemePayload setThemeName(final String themeName) {
        this.themeName = themeName;
        return this;
    }

    public List<LearnerThemeVariant> getThemeVariants() {
        return themeVariants;
    }

    public LearnerSelectedThemePayload setThemeVariants(final List<LearnerThemeVariant> themeVariants) {
        this.themeVariants = themeVariants;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerSelectedThemePayload that = (LearnerSelectedThemePayload) o;
        return Objects.equals(elementId, that.elementId) &&
                Objects.equals(themeId, that.themeId) &&
                Objects.equals(themeName, that.themeName) &&
                Objects.equals(themeVariants, that.themeVariants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, themeId, themeName, themeVariants);
    }

    @Override
    public String toString() {
        return "LearnerSelectedThemePayload{" +
                "elementId=" + elementId +
                ", themeId=" + themeId +
                ", themeName='" + themeName + '\'' +
                ", themeVariants=" + themeVariants +
                '}';
    }
}
