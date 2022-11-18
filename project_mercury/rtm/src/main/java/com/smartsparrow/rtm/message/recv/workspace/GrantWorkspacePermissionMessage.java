package com.smartsparrow.rtm.message.recv.workspace;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GrantWorkspacePermissionMessage extends ReceivedMessage implements WorkspaceMessage {

    private List<UUID> accountIds;
    private List<UUID> teamIds;
    private UUID workspaceId;
    private PermissionLevel permissionLevel;

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public List<UUID> getAccountIds() {
        return accountIds;
    }

    public List<UUID> getTeamIds() {
        return teamIds;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantWorkspacePermissionMessage that = (GrantWorkspacePermissionMessage) o;
        return Objects.equals(accountIds, that.accountIds) &&
                Objects.equals(teamIds, that.teamIds) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountIds, teamIds, workspaceId, permissionLevel);
    }

    @Override
    public String toString() {
        return "GrantWorkspacePermissionMessage{" +
                "accountIds=" + accountIds +
                ", teamIds=" + teamIds +
                ", workspaceId=" + workspaceId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
