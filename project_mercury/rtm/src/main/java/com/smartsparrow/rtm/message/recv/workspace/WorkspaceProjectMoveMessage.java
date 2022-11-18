package com.smartsparrow.rtm.message.recv.workspace;

import com.smartsparrow.rtm.message.ReceivedMessage;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class WorkspaceProjectMoveMessage extends ReceivedMessage implements ProjectMessage  {

    private UUID projectId;
    private UUID workspaceId;

    @Override
    public UUID getProjectId() { return projectId; }

    public UUID getWorkspaceId() { return workspaceId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceProjectMoveMessage that = (WorkspaceProjectMoveMessage) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, workspaceId);
    }

    @Override
    public String toString() {
        return "WorkspaceProjectMoveMessage{" +
                "projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
