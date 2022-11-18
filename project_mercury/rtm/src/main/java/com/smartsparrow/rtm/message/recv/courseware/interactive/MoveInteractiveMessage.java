package com.smartsparrow.rtm.message.recv.courseware.interactive;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;
import com.smartsparrow.rtm.message.recv.courseware.pathway.PathwayMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class MoveInteractiveMessage extends ReceivedMessage implements CoursewareElementMessage, PathwayMessage {

    private UUID interactiveId;
    private UUID pathwayId;
    private Integer index;

    public UUID getInteractiveId() { return interactiveId; }

    @Override
    public UUID getPathwayId() { return pathwayId; }

    public Integer getIndex() { return index; }

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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveInteractiveMessage that = (MoveInteractiveMessage) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(pathwayId, that.pathwayId) &&
                Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() { return Objects.hash(interactiveId, pathwayId, index); }

    @Override
    public String toString() {
        return "MoveInteractiveMessage{" +
                "interactiveId=" + interactiveId +
                ", pathwayId=" + pathwayId +
                ", index=" + index +
                '}';
    }
}
