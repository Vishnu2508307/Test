package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public abstract class PluginCollaborator {

    private UUID pluginId;
    private PermissionLevel permissionLevel;

    public UUID getPluginId() {
        return pluginId;
    }

    public PluginCollaborator setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public PluginCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginCollaborator that = (PluginCollaborator) o;
        return Objects.equals(pluginId, that.pluginId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, permissionLevel);
    }

    @Override
    public String toString() {
        return "PluginCollaborator{" +
                "pluginId=" + pluginId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
