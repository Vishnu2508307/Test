package com.smartsparrow.iam.data.permission.team;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class TeamPermission {
    private UUID accountId;
    private UUID teamId;
    private PermissionLevel permissionLevel;

    public UUID getAccountId() {
        return accountId;
    }

    public TeamPermission setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public TeamPermission setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public TeamPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamPermission that = (TeamPermission) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(teamId, that.teamId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, teamId, permissionLevel);
    }

    @Override
    public String toString() {
        return "TeamPermission{" +
                "accountId=" + accountId +
                ", teamId=" + teamId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
