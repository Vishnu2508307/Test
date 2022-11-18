package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteActivityMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID activityId;
    private UUID parentPathwayId;

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getParentPathwayId() {
        return parentPathwayId;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteActivityMessage that = (DeleteActivityMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(parentPathwayId, that.parentPathwayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, parentPathwayId);
    }

    @Override
    public String toString() {
        return "DeleteActivityMessage{" +
                "activityId=" + activityId +
                ", parentPathwayId=" + parentPathwayId +
                '}';
    }
}
