package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class ThemeCoursewareElement {

    private UUID elementId;
    private UUID themeId;
    private CoursewareElementType elementType;

    public UUID getElementId() { return elementId; }

    public ThemeCoursewareElement setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getThemeId() { return themeId; }

    public ThemeCoursewareElement setThemeId(final UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ThemeCoursewareElement setElementType(final CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeCoursewareElement that = (ThemeCoursewareElement) o;
        return Objects.equals(elementId, that.elementId) &&
                Objects.equals(themeId, that.themeId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, themeId, elementType);
    }

    @Override
    public String toString() {
        return "ThemeCoursewareElement{" +
                "elementId=" + elementId +
                ", themeId=" + themeId +
                ", elementType=" + elementType +
                '}';
    }
}
