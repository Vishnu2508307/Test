package com.smartsparrow.rtm.message.recv.team;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class TeamPermissionMessage extends ReceivedMessage implements TeamMessage {

    private List<UUID> accountIds;
    private PermissionLevel permissionLevel;
    private UUID teamId;

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    @Override
    public UUID getTeamId() {
        return teamId;
    }

    public List<UUID> getAccountIds() {
        return accountIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TeamPermissionMessage that = (TeamPermissionMessage) o;
        return Objects.equals(accountIds, that.accountIds) &&
                permissionLevel == that.permissionLevel &&
                Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountIds, permissionLevel, teamId);
    }
}
