package com.smartsparrow.rtm.message.recv.courseware.feedback;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ReplaceInteractiveFeedbackConfigMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID feedbackId;
    private String config;

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public String getConfig() {
        return config;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return feedbackId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.FEEDBACK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceInteractiveFeedbackConfigMessage that = (ReplaceInteractiveFeedbackConfigMessage) o;
        return Objects.equals(feedbackId, that.feedbackId) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {

        return Objects.hash(feedbackId, config);
    }

    @Override
    public String toString() {
        return "ReplaceInteractiveFeedbackConfigMessage{" +
                "feedbackId=" + feedbackId +
                ", config='" + config + '\'' +
                '}';
    }
}
