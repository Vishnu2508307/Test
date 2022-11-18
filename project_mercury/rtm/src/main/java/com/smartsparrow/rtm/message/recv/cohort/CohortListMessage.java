package com.smartsparrow.rtm.message.recv.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CohortListMessage extends ReceivedMessage {

    private UUID workspaceId;

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public CohortListMessage setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortListMessage that = (CohortListMessage) o;
        return Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId);
    }

    @Override
    public String toString() {
        return "CohortListMessage{" +
                "workspaceId=" + workspaceId +
                "} " + super.toString();
    }
}
