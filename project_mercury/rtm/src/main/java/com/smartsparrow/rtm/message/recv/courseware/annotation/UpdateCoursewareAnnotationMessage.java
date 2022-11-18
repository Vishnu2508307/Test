package com.smartsparrow.rtm.message.recv.courseware.annotation;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.AnnotationMessage;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class UpdateCoursewareAnnotationMessage extends ReceivedMessage implements AnnotationMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private UUID annotationId;
    private String body;
    private String target;

    @Override
    public UUID getAnnotationId() {
        return annotationId;
    }

    public String getBody() {
        return body;
    }

    public String getTarget() {
        return target;
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
        UpdateCoursewareAnnotationMessage that = (UpdateCoursewareAnnotationMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(annotationId, that.annotationId) &&
                Objects.equals(body, that.body) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, annotationId, body, target);
    }

    @Override
    public String toString() {
        return "UpdateCoursewareAnnotationMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", annotationId=" + annotationId +
                ", body='" + body + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
