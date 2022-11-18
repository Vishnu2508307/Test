package com.smartsparrow.rtm.message.recv.courseware;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GetCoursewareElementMetaInformationMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private String key;

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetCoursewareElementMetaInformationMessage that = (GetCoursewareElementMetaInformationMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, key);
    }

    @Override
    public String toString() {
        return "GetCoursewareElementMetaInformationMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", key='" + key + '\'' +
                '}';
    }
}
