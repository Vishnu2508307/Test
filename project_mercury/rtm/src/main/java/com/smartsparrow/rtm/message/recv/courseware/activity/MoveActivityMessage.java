package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;
import com.smartsparrow.rtm.message.recv.courseware.pathway.PathwayMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class MoveActivityMessage extends ReceivedMessage implements CoursewareElementMessage, PathwayMessage {

    private UUID activityId;
    private UUID pathwayId;
    private Integer index;

    public UUID getActivityId() {
        return activityId;
    }

    public Integer getIndex() {
        return index;
    }

    @Override
    public UUID getPathwayId() {
        return pathwayId;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return activityId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.ACTIVITY;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveActivityMessage that = (MoveActivityMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(pathwayId, that.pathwayId) &&
                Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, pathwayId, index);
    }

    @Override
    public String toString() {
        return "MoveActivityMessage{" +
                "activityId=" + activityId +
                ", pathwayId=" + pathwayId +
                ", index=" + index +
                '}';
    }
}
