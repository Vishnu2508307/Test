package com.smartsparrow.rtm.message.recv.courseware.component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteInteractiveComponentsMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID interactiveId;
    private List<UUID> componentIds;

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public List<UUID> getComponentIds() {
        return componentIds;
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
        DeleteInteractiveComponentsMessage that = (DeleteInteractiveComponentsMessage) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(componentIds, that.componentIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveId, componentIds);
    }

    @Override
    public String toString() {
        return "DeleteInteractiveComponentsMessage{" +
                "interactiveId=" + interactiveId +
                ", componentIds=" + componentIds +
                '}';
    }
}
