package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class RevokeWorkspacePermissionMessage extends ReceivedMessage implements WorkspaceMessage {

    private UUID workspaceId;
    private UUID teamId;
    private UUID accountId;

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevokeWorkspacePermissionMessage that = (RevokeWorkspacePermissionMessage) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(teamId, that.teamId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, teamId, accountId);
    }

    @Override
    public String toString() {
        return "RevokeWorkspacePermissionMessage{" +
                "workspaceId=" + workspaceId +
                ", teamId=" + teamId +
                ", accountId=" + accountId +
                '}';
    }
}
