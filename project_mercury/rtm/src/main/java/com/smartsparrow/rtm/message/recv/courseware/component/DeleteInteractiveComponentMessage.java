package com.smartsparrow.rtm.message.recv.courseware.component;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteInteractiveComponentMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID interactiveId;
    private UUID componentId;

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public UUID getComponentId() {
        return componentId;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return interactiveId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.INTERACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteInteractiveComponentMessage that = (DeleteInteractiveComponentMessage) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(componentId, that.componentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveId, componentId);
    }

    @Override
    public String toString() {
        return "DeleteInteractiveComponentMessage{" +
                "interactiveId=" + interactiveId +
                ", componentId=" + componentId +
                '}';
    }
}
