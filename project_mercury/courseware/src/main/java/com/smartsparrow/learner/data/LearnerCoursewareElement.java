package com.smartsparrow.learner.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class LearnerCoursewareElement implements Serializable {

    private static final long serialVersionUID = 8113612765131056378L;
    private UUID id;
    private CoursewareElementType elementType;
    private UUID deploymentId;
    private UUID changeId;

    public UUID getId() {
        return id;
    }

    public LearnerCoursewareElement setId(UUID id) {
        this.id = id;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public LearnerCoursewareElement setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerCoursewareElement setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerCoursewareElement setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerCoursewareElement that = (LearnerCoursewareElement) o;
        return Objects.equals(id, that.id) &&
                elementType == that.elementType &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, elementType, deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "LearnerCoursewareElement{" +
                "id=" + id +
                ", elementType=" + elementType +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                '}';
    }
}
