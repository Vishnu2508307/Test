package com.smartsparrow.rtm.message.recv.courseware;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GetCoursewareElementStructureNavigateMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private List<String> fieldNames;

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetCoursewareElementStructureNavigateMessage that = (GetCoursewareElementStructureNavigateMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(fieldNames, that.fieldNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, fieldNames);
    }

    @Override
    public String toString() {
        return "GetCoursewareElementStructureNavigateMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", fieldNames=" + fieldNames +
                '}';
    }
}
