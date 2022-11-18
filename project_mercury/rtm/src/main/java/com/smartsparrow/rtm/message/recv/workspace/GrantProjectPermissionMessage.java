package com.smartsparrow.rtm.message.recv.workspace;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GrantProjectPermissionMessage extends ReceivedMessage implements ProjectMessage {

    private List<UUID> accountIds;
    private List<UUID> teamIds;
    private UUID projectId;
    private PermissionLevel permissionLevel;

    @Override
    public UUID getProjectId() {
        return projectId;
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantProjectPermissionMessage that = (GrantProjectPermissionMessage) o;
        return Objects.equals(accountIds, that.accountIds) &&
                Objects.equals(teamIds, that.teamIds) &&
                Objects.equals(projectId, that.projectId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountIds, teamIds, projectId, permissionLevel);
    }

    @Override
    public String toString() {
        return "GrantProjectPermissionMessage{" +
                "accountIds=" + accountIds +
                ", teamIds=" + teamIds +
                ", projectId=" + projectId +
                ", permissionLevel=" + permissionLevel +
                "}" + super.toString();
    }
}
