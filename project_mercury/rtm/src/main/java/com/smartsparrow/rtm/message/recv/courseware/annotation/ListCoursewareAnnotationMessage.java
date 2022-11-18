package com.smartsparrow.rtm.message.recv.courseware.annotation;

import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.CoursewareElementMotivationMessage;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListCoursewareAnnotationMessage extends ReceivedMessage implements CoursewareElementMotivationMessage {

    private UUID rootElementId;
    private UUID elementId;
    private CoursewareElementType elementType;
    private Motivation motivation;

    public UUID getRootElementId() {
        return rootElementId;
    }

    @Override
    public Motivation getMotivation() {
        return motivation;
    }
    
    @Nullable
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
        ListCoursewareAnnotationMessage that = (ListCoursewareAnnotationMessage) o;
        return Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                motivation == that.motivation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElementId, elementId, elementType, motivation);
    }

    @Override
    public String toString() {
        return "GetCoursewareAnnotationMessage{" +
                "rootElementId=" + rootElementId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", motivation=" + motivation +
                '}';
    }
}
