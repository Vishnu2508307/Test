package com.smartsparrow.rtm.message.recv.courseware.annotation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.annotation.service.CoursewareAnnotationKey;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ResolveCoursewareAnnotationMessage extends ReceivedMessage {

    private UUID rootElementId;
    private List<CoursewareAnnotationKey> coursewareAnnotationKeys;
    private Boolean resolved;

    public UUID getRootElementId() {
        return rootElementId;
    }

    public ResolveCoursewareAnnotationMessage setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public List<CoursewareAnnotationKey> getCoursewareAnnotationKeys() {
        return coursewareAnnotationKeys;
    }

    public ResolveCoursewareAnnotationMessage setCoursewareAnnotationKeys(final List<CoursewareAnnotationKey> coursewareAnnotationKeys) {
        this.coursewareAnnotationKeys = coursewareAnnotationKeys;
        return this;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public ResolveCoursewareAnnotationMessage setResolved(final Boolean resolved) {
        this.resolved = resolved;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolveCoursewareAnnotationMessage that = (ResolveCoursewareAnnotationMessage) o;
        return Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(coursewareAnnotationKeys, that.coursewareAnnotationKeys) &&
                Objects.equals(resolved, that.resolved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElementId, coursewareAnnotationKeys, resolved);
    }

    @Override
    public String toString() {
        return "ResolveCoursewareAnnotationMessage{" +
                "rootElementId=" + rootElementId +
                ", coursewareAnnotations=" + coursewareAnnotationKeys +
                ", resolved=" + resolved +
                '}';
    }
}
