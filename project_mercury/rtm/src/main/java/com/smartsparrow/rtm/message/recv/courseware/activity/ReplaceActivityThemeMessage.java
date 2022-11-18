package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ReplaceActivityThemeMessage extends ReceivedMessage implements CoursewareElementMessage {

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceActivityThemeMessage that = (ReplaceActivityThemeMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {

        return Objects.hash(activityId, config);
    }

    @Override
    public String toString() {
        return "ReplaceActivityThemeMessage{" +
                "activityId=" + activityId +
                ", config='" + config + '\'' +
                '}';
    }
}
