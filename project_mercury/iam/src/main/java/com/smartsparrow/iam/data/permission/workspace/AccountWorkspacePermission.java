package com.smartsparrow.iam.data.permission.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class AccountWorkspacePermission {

    private UUID accountId;
    private UUID workspaceId;
    private PermissionLevel permissionLevel;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountWorkspacePermission setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public AccountWorkspacePermission setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public AccountWorkspacePermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountWorkspacePermission that = (AccountWorkspacePermission) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, workspaceId, permissionLevel);
    }

    @Override
    public String toString() {
        return "AccountWorkspacePermission{" +
                "accountId=" + accountId +
                ", workspaceId=" + workspaceId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
