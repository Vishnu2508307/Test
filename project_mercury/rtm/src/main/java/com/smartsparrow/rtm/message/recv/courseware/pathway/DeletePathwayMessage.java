package com.smartsparrow.rtm.message.recv.courseware.pathway;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeletePathwayMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID pathwayId;
    private UUID parentActivityId;

    public UUID getPathwayId() {
        return pathwayId;
    }

    public UUID getParentActivityId() {
        return parentActivityId;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return pathwayId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.PATHWAY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletePathwayMessage that = (DeletePathwayMessage) o;
        return Objects.equals(pathwayId, that.pathwayId) &&
                Objects.equals(parentActivityId, that.parentActivityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayId, parentActivityId);
    }

    @Override
    public String toString() {
        return "DeletePathwayMessage{" +
                "pathwayId=" + pathwayId +
                ", parentActivityId=" + parentActivityId +
                '}';
    }
}
