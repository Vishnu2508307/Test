package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ReplaceActivityConfigMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID activityId;
    private String config;

    public UUID getActivityId() {
        return activityId;
    }

    public String getConfig() {
        return config;
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ReplaceActivityConfigMessage that = (ReplaceActivityConfigMessage) o;
        return Objects.equal(activityId, that.activityId) && Objects.equal(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(activityId, config);
    }
}

