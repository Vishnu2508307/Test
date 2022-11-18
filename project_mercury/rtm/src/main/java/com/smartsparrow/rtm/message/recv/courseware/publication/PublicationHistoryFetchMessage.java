package com.smartsparrow.rtm.message.recv.courseware.publication;

import com.smartsparrow.publication.data.PublicationOutputType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.UUID;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PublicationHistoryFetchMessage extends ReceivedMessage {

    private UUID activityId;
    private PublicationOutputType outputType;

    public UUID getActivityId() {
        return activityId;
    }

    public PublicationOutputType getOutputType() {
        return outputType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationHistoryFetchMessage that = (PublicationHistoryFetchMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                outputType == that.getOutputType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, outputType);
    }

    @Override
    public String toString() {
        return "PublicationHistoryFetchMessage{" +
                "activityId=" + activityId +
                ", outputType=" + outputType +
                '}';
    }
}
