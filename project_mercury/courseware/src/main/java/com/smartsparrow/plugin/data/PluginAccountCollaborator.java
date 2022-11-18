package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class PluginAccountCollaborator extends PluginCollaborator {

    private UUID accountId;

    public UUID getAccountId() {
        return accountId;
    }

    public PluginAccountCollaborator setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public PluginAccountCollaborator setPluginId(UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public PluginAccountCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PluginAccountCollaborator that = (PluginAccountCollaborator) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountId);
    }

    @Override
    public String toString() {
        return "PluginAccountCollaborator{" +
                "accountId=" + accountId +
                "} " + super.toString();
    }
}
