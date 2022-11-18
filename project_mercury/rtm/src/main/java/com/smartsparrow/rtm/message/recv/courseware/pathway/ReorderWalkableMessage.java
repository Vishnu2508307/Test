package com.smartsparrow.rtm.message.recv.courseware.pathway;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ReorderWalkableMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID pathwayId;
    private List<UUID> walkableIds;

    public UUID getPathwayId() {
        return pathwayId;
    }

    public List<UUID> getWalkableIds() {
        return walkableIds;
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
        ReorderWalkableMessage that = (ReorderWalkableMessage) o;
        return Objects.equals(pathwayId, that.pathwayId) &&
                Objects.equals(walkableIds, that.walkableIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayId, walkableIds);
    }

    @Override
    public String toString() {
        return "ReorderWalkableMessage{" +
                "pathwayId=" + pathwayId +
                ", walkableIds=" + walkableIds +
                "} " + super.toString();
    }
}
