package com.smartsparrow.courseware.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CoursewareElementAncestry {

    private UUID elementId;
    private CoursewareElementType type;
    private List<CoursewareElement> ancestry;

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementAncestry setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getType() {
        return type;
    }

    public CoursewareElementAncestry setType(CoursewareElementType type) {
        this.type = type;
        return this;
    }

    public List<CoursewareElement> getAncestry() {
        return ancestry;
    }

    public CoursewareElementAncestry setAncestry(List<CoursewareElement> ancestry) {
        this.ancestry = ancestry;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElementAncestry that = (CoursewareElementAncestry) o;
        return Objects.equals(elementId, that.elementId) &&
                type == that.type &&
                Objects.equals(ancestry, that.ancestry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, type, ancestry);
    }
}
