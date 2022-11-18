package com.smartsparrow.iam.data.permission.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

/**
 * Defines plugin permission for account in workspace context
 */
public class AccountPluginPermission {

    private UUID pluginId;
    private PermissionLevel permissionLevel;
    private UUID accountId;

    public UUID getPluginId() {
        return pluginId;
    }

    public AccountPluginPermission setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public AccountPluginPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public AccountPluginPermission setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountPluginPermission that = (AccountPluginPermission) o;
        return Objects.equals(pluginId, that.pluginId) &&
                permissionLevel == that.permissionLevel &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pluginId, permissionLevel, accountId);
    }

    @Override
    public String toString() {
        return "AccountPluginPermission{" +
                "pluginId=" + pluginId +
                ", permissionLevel=" + permissionLevel +
                ", accountId=" + accountId +
                '}';
    }
}
