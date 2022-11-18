package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
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
        WorkspaceGenericMessage that = (WorkspaceGenericMessage) o;
        return Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(workspaceId);
    }
}
