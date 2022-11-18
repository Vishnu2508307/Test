package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "the field is mapped from the websocket message")
public class WorkspaceGenericMessage extends ReceivedMessage implements WorkspaceMessage {

    private UUID workspaceId;

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkspaceGenericMessage that = (WorkspaceGenericMessage) o;
        return Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), workspaceId);
    }

    @Override
    public String toString() {
        return "WorkspaceGenericMessage{" +
                "workspaceId=" + workspaceId +
                '}';
    }
}
