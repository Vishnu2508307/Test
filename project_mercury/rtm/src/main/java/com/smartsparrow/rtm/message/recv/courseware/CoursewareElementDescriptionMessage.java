package com.smartsparrow.rtm.message.recv.courseware;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CoursewareElementDescriptionMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private String description;

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElementDescriptionMessage that = (CoursewareElementDescriptionMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, description);
    }

    @Override
    public String toString() {
        return "CoursewareElementDescriptionMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", description='" + description + '\'' +
                '}';
    }
}
