package com.smartsparrow.rtm.message.recv.courseware.interactive;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ReplaceInteractiveConfigMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID interactiveId;
    private String config;

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public String getConfig() {
        return config;
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ReplaceInteractiveConfigMessage that = (ReplaceInteractiveConfigMessage) o;
        return Objects.equal(interactiveId, that.interactiveId) && Objects.equal(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(interactiveId, config);
    }

    @Override
    public String toString() {
        return "ReplaceInteractiveConfigMessage{" +
                "interactiveId=" + interactiveId +
                ", config='" + config + '\'' +
                "} " + super.toString();
    }
}

