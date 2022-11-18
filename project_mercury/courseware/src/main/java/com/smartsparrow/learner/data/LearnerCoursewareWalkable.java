package com.smartsparrow.learner.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;


public class LearnerCoursewareWalkable {
    private UUID elementId;
    private CoursewareElementType type;
    private List<LearnerCoursewareWalkable> children;
    private UUID topParentId;
    private UUID parentId;
    private LearnerWalkablePayload learnerWalkablePayload;

    public LearnerCoursewareWalkable() {
        this.children = new ArrayList<>();
    }

    public UUID getElementId() {
        return elementId;
    }

    public LearnerCoursewareWalkable setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getType() {
        return type;
    }

    public LearnerCoursewareWalkable setType(final CoursewareElementType type) {
        this.type = type;
        return this;
    }

    public List<LearnerCoursewareWalkable> getChildren() {
        return children;
    }

    public LearnerCoursewareWalkable addChild(final LearnerCoursewareWalkable child) {
        children.add(child);
        return this;
    }

    public UUID getTopParentId() {
        return topParentId;
    }

    public LearnerCoursewareWalkable setTopParentId(final UUID topParentId) {
        this.topParentId = topParentId;
        return this;
    }

    public UUID getParentId() {
        return parentId;
    }

    public LearnerCoursewareWalkable setParentId(final UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public LearnerWalkablePayload getLearnerWalkablePayload() {
        return learnerWalkablePayload;
    }

    public LearnerCoursewareWalkable setLearnerWalkablePayload(final LearnerWalkablePayload learnerWalkablePayload) {
        this.learnerWalkablePayload = learnerWalkablePayload;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerCoursewareWalkable that = (LearnerCoursewareWalkable) o;
        return Objects.equals(elementId, that.elementId) &&
                type == that.type &&
                Objects.equals(children, that.children) &&
                Objects.equals(topParentId, that.topParentId) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(learnerWalkablePayload, that.learnerWalkablePayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId,
                            type,
                            children,
                            topParentId,
                            parentId,
                            learnerWalkablePayload);
    }

    @Override
    public String toString() {
        return "LearnerCoursewareWalkable{" +
                "elementId=" + elementId +
                ", type=" + type +
                ", children=" + children +
                ", topParentId=" + topParentId +
                ", parentId=" + parentId +
                ", learnerWalkablePayload=" + learnerWalkablePayload +
                '}';
    }
}
