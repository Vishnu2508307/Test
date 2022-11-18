package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

public class LearnerThemeByElement {
    private UUID elementId;
    private UUID themeId;
    private String themeName;

    public UUID getElementId() {
        return elementId;
    }

    public LearnerThemeByElement setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getThemeId() {
        return themeId;
    }

    public LearnerThemeByElement setThemeId(final UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public String getThemeName() {
        return themeName;
    }

    public LearnerThemeByElement setThemeName(final String themeName) {
        this.themeName = themeName;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerThemeByElement that = (LearnerThemeByElement) o;
        return Objects.equals(elementId, that.elementId) &&
                Objects.equals(themeId, that.themeId) &&
                Objects.equals(themeName, that.themeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, themeId, themeName);
    }

    @Override
    public String toString() {
        return "LearnerThemeByElement{" +
                "elementId=" + elementId +
                ", themeId=" + themeId +
                ", themeName='" + themeName + '\'' +
                '}';
    }
}
