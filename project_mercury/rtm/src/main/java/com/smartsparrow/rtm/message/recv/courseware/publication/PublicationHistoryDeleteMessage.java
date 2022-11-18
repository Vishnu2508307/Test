package com.smartsparrow.rtm.message.recv.courseware.publication;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PublicationHistoryDeleteMessage extends ReceivedMessage {

    private UUID publicationId;

    private UUID activityId;

    private String version;

    public UUID getPublicationId() {
        return publicationId;
    }
    public String getVersion() {
        return version;
    }
    public UUID getActivityId() {
        return activityId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationHistoryDeleteMessage that = (PublicationHistoryDeleteMessage) o;
        return Objects.equals(publicationId, that.publicationId) && Objects.equals(version, this.version) && Objects.equals(activityId, this.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId, activityId, version);
    }


    @Override
    public String toString() {
        return "PublicationHistoryDeleteMessage{" +
                "publicationId=" + publicationId +
                "activityId=" + activityId +
                "version=" + version +
                '}';
    }
}
