package com.smartsparrow.rtm.message.recv.courseware;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SetCoursewareElementMetaInformationMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private String key;
    private String value;

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetCoursewareElementMetaInformationMessage that = (SetCoursewareElementMetaInformationMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, key, value);
    }

    @Override
    public String toString() {
        return "SetCoursewareElementMetaInformationMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
