package com.smartsparrow.la.event;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.courseware.data.CoursewareElementType;

public class ContentSeedingMessage extends CohortBroadcastMessage {
    private static final long serialVersionUID = 4760385098071367472L;
    private UUID deploymentId;
    private UUID changeId;
    private UUID elementId;
    private CoursewareElementType coursewareElementType;

    public ContentSeedingMessage(UUID cohortId) {
        super(cohortId);
    }

    @Override
    public UUID getCohortId() {
        return super.getCohortId();
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public ContentSeedingMessage setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public ContentSeedingMessage setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public ContentSeedingMessage setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public ContentSeedingMessage setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ContentSeedingMessage that = (ContentSeedingMessage) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(elementId, that.elementId) &&
                coursewareElementType == that.coursewareElementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId, elementId, coursewareElementType);
    }

    @Override
    public String toString() {
        return "ContentSeedingMessage{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", elementId=" + elementId +
                ", coursewareElementType=" + coursewareElementType +
                "} " + super.toString();
    }
}
