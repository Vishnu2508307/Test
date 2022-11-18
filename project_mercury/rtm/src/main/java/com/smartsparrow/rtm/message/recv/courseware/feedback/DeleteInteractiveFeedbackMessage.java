package com.smartsparrow.rtm.message.recv.courseware.feedback;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteInteractiveFeedbackMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID feedbackId;
    private UUID interactiveId;

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public UUID getInteractiveId() {
        return interactiveId;
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
        DeleteInteractiveFeedbackMessage that = (DeleteInteractiveFeedbackMessage) o;
        return Objects.equals(feedbackId, that.feedbackId) &&
                Objects.equals(interactiveId, that.interactiveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackId, interactiveId);
    }

    @Override
    public String toString() {
        return "DeleteInteractiveFeedbackMessage{" +
                "feedbackId=" + feedbackId +
                ", interactiveId=" + interactiveId +
                "} " + super.toString();
    }
}
