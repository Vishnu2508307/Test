package com.smartsparrow.rtm.message.recv.courseware.publication;

import com.smartsparrow.rtm.message.ReceivedMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.UUID;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PublicationActivityFetchMessage extends ReceivedMessage {

    private UUID workspaceId;

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationActivityFetchMessage that = (PublicationActivityFetchMessage) o;
        return Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId);
    }

    @Override
    public String toString() {
        return "PublicationActivityFetchMessage{" +
                "workspaceId=" + workspaceId +
                '}';
    }
}
