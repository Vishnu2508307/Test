package com.smartsparrow.iam.data.permission.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class AccountProjectPermission {

    private UUID projectId;
    private UUID accountId;
    private PermissionLevel permissionLevel;

    public UUID getProjectId() {
        return projectId;
    }

    public AccountProjectPermission setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public AccountProjectPermission setAccountId(final UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public AccountProjectPermission setPermissionLevel(final PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountProjectPermission that = (AccountProjectPermission) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(accountId, that.accountId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, accountId, permissionLevel);
    }

    @Override
    public String toString() {
        return "ProjectAccountPermission{" +
                "projectId=" + projectId +
                ", accountId=" + accountId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
