package com.smartsparrow.courseware.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class WalkableChild implements Serializable {

    private static final long serialVersionUID = 6846113030910406734L;

    private UUID elementId;
    private CoursewareElementType elementType;

    public UUID getElementId() {
        return elementId;
    }

    public WalkableChild setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public WalkableChild setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalkableChild that = (WalkableChild) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType);
    }

    @Override
    public String toString() {
        return "WalkableChild{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
