package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListWorkspaceCollaboratorMessage extends ReceivedMessage implements WorkspaceMessage {

    private UUID workspaceId;
    private Integer limit;

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListWorkspaceCollaboratorMessage that = (ListWorkspaceCollaboratorMessage) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, limit);
    }

    @Override
    public String toString() {
        return "ListWorkspaceCollaboratorMessage{" +
                "workspaceId=" + workspaceId +
                ", limit=" + limit +
                "} " + super.toString();
    }
}
