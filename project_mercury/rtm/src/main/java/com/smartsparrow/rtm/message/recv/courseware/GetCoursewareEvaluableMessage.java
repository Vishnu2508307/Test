package com.smartsparrow.rtm.message.recv.courseware;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;

public class GetCoursewareEvaluableMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;

    public UUID getElementId() { return elementId; }

    @Override
    public CoursewareElementType getElementType() { return elementType; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetCoursewareEvaluableMessage that = (GetCoursewareEvaluableMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType);
    }

    @Override
    public String toString() {
        return "GetCoursewareEvaluableMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
