package com.smartsparrow.rtm.message.recv.courseware.annotation;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.CoursewareElementMotivationMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateCoursewareAnnotationMessage extends ReceivedMessage implements CoursewareElementMotivationMessage {

    private UUID rootElementId;
    private UUID elementId;
    private CoursewareElementType elementType;
    private Motivation motivation;
    private String body;
    private String target;
    private UUID annotationId;

    public UUID getRootElementId() {
        return rootElementId;
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
    public Motivation getMotivation() {
        return motivation;
    }

    public String getBody() {
        return body;
    }

    public String getTarget() {
        return target;
    }

    public UUID getAnnotationId() {
        return annotationId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateCoursewareAnnotationMessage that = (CreateCoursewareAnnotationMessage) o;
        return Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                motivation == that.motivation &&
                Objects.equals(body, that.body) &&
                Objects.equals(target, that.target) &&
                Objects.equals(annotationId, that.annotationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElementId, elementId, elementType, motivation, body, target, annotationId);
    }

    @Override
    public String toString() {
        return "CreateCoursewareAnnotationMessage{" +
                "rootElementId=" + rootElementId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", motivation=" + motivation +
                ", body='" + body + '\'' +
                ", target='" + target + '\'' +
                ", annotationId=" + annotationId +
                '}';
    }
}
