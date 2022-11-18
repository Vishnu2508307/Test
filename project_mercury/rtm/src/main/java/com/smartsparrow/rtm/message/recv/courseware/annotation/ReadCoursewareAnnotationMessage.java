package com.smartsparrow.rtm.message.recv.courseware.annotation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ReadCoursewareAnnotationMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID rootElementId;
    private UUID elementId;
    private CoursewareElementType elementType;
    private List<UUID> annotationIds;
    private Boolean read;

    public UUID getRootElementId() {
        return rootElementId;
    }

    public ReadCoursewareAnnotationMessage setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    @Override
    public UUID getElementId() {
        return elementId;
    }

    public ReadCoursewareAnnotationMessage setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ReadCoursewareAnnotationMessage setElementType(final CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public List<UUID> getAnnotationIds() {
        return annotationIds;
    }

    public ReadCoursewareAnnotationMessage setCoursewareAnnotationKeys(final List<UUID> annotationIds) {
        this.annotationIds = annotationIds;
        return this;
    }

    public Boolean getRead() {
        return read;
    }

    public ReadCoursewareAnnotationMessage setRead(final Boolean read) {
        this.read = read;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReadCoursewareAnnotationMessage that = (ReadCoursewareAnnotationMessage) o;
        return Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(elementType, that.elementType) &&
                Objects.equals(annotationIds, that.annotationIds) &&
                Objects.equals(read, that.read);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElementId, elementId, elementType, annotationIds, read);
    }

    @Override
    public String toString() {
        return "ReadCoursewareAnnotationMessage {" +
                "rootElementId=" + rootElementId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", annotationIds=" + annotationIds +
                ", read=" + read +
                '}';
    }
}
