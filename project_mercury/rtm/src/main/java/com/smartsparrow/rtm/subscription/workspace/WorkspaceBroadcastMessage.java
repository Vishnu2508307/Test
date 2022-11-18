package com.smartsparrow.rtm.subscription.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class WorkspaceBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -1488395417684857059L;
    protected final UUID workspaceId;

    public WorkspaceBroadcastMessage(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceBroadcastMessage that = (WorkspaceBroadcastMessage) o;
        return Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId);
    }

    @Override
    public String toString() {
        return "WorkspaceBroadcastMessage{" +
                "workspaceId=" + workspaceId +
                '}';
    }
}
