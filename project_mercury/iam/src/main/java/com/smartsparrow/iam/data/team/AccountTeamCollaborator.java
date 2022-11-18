package com.smartsparrow.iam.data.team;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class AccountTeamCollaborator {
    private UUID accountId;
    private UUID teamId;
    private PermissionLevel permissionLevel;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountTeamCollaborator setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public AccountTeamCollaborator setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public AccountTeamCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountTeamCollaborator that = (AccountTeamCollaborator) o;
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
        return "AccountTeamCollaborator{" +
                "accountId=" + accountId +
                ", teamId=" + teamId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
