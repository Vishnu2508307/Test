package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class CoursewareElementDescription {

    private UUID elementId;
    private CoursewareElementType elementType;
    private String value;

    public CoursewareElementDescription() {
    }

    public CoursewareElementDescription(UUID elementId, CoursewareElementType elementType, String value) {
        this.elementId = elementId;
        this.elementType = elementType;
        this.value = value;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementDescription setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public CoursewareElementDescription setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public String getValue() {
        return value;
    }

    public CoursewareElementDescription setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElementDescription that = (CoursewareElementDescription) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, value);
    }

    @Override
    public String toString() {
        return "CoursewareElementDescription{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", value='" + value + '\'' +
                '}';
    }
}
