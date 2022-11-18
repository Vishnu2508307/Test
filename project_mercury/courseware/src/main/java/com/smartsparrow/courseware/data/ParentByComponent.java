package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class ParentByComponent {

    private UUID componentId;
    private UUID parentId;
    private CoursewareElementType parentType;

    public UUID getComponentId() {
        return componentId;
    }

    public ParentByComponent setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    public UUID getParentId() {
        return parentId;
    }

    public ParentByComponent setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    public ParentByComponent setParentType(CoursewareElementType parentType) {
        this.parentType = parentType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentByComponent that = (ParentByComponent) o;
        return Objects.equals(componentId, that.componentId) &&
                Objects.equals(parentId, that.parentId) &&
                parentType == that.parentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, parentId, parentType);
    }
}
