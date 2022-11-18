package com.smartsparrow.courseware.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

public class CoursewareElement implements Serializable {

    private static final long serialVersionUID = 3107833509119643049L;

    private UUID elementId;
    private CoursewareElementType elementType;

    public CoursewareElement() {
    }

    public CoursewareElement(UUID elementId, CoursewareElementType elementType) {
        this.elementId = elementId;
        this.elementType = elementType;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElement setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public CoursewareElement setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public static CoursewareElement from(@Nonnull UUID elementId, @Nonnull CoursewareElementType type) {
        return new CoursewareElement()
                .setElementId(elementId)
                .setElementType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElement that = (CoursewareElement) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType);
    }

    @Override
    public String toString() {
        return "CoursewareElement{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
