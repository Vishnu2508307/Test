package com.smartsparrow.rtm.message.recv.courseware.annotation;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.AnnotationMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteCoursewareAnnotationMessage extends ReceivedMessage implements AnnotationMessage {

    private UUID annotationId;
    private UUID elementId;
    private CoursewareElementType elementType;

    @Override
    public UUID getAnnotationId() {
        return annotationId;
    }

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteCoursewareAnnotationMessage that = (DeleteCoursewareAnnotationMessage) o;
        return Objects.equals(annotationId, that.annotationId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(elementType, that.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationId, elementId, elementType);
    }

    @Override
    public String toString() {
        return "DeleteCoursewareAnnotationMessage {" +
                "annotationId=" + annotationId +
                ", elementId='" + elementId + '\'' +
                ", elementType='" + elementType + '\'' +
                '}';
    }
}
