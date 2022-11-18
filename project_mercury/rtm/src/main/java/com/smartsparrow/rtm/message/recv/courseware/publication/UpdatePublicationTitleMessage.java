package com.smartsparrow.rtm.message.recv.courseware.publication;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class UpdatePublicationTitleMessage extends ReceivedMessage {

    private UUID activityId;
    private String title;
    private String version;

    public UUID getActivityId() {
        return activityId;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdatePublicationTitleMessage that = (UpdatePublicationTitleMessage) o;
        return Objects.equals(activityId, that.activityId) && Objects.equals(title,
                                                                             that.title) && Objects.equals(
                version,
                that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, title, version);
    }

    @Override
    public String toString() {
        return "UpdatePublicationTitleMessage{" +
                "activityId=" + activityId +
                ", title='" + title + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
