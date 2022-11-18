package com.smartsparrow.rtm.message.recv.courseware.component;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class MoveComponentsMessage extends ReceivedMessage implements CoursewareElementMessage {

    private List<UUID> componentIds;
    private UUID elementId;
    private CoursewareElementType elementType;

    public List<UUID> getComponentIds() {
        return componentIds;
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
        MoveComponentsMessage that = (MoveComponentsMessage) o;
        return Objects.equals(componentIds, that.componentIds) && Objects.equals(elementId, that.elementId) && elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentIds, elementId, elementType);
    }

    @Override
    public String toString() {
        return "MoveComponentMessage{" +
                "componentIds=" + componentIds +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
