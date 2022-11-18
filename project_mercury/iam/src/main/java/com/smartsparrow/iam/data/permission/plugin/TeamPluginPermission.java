package com.smartsparrow.iam.data.permission.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

/**
 * It defines what permission level has team on a plugin
 */
public class TeamPluginPermission {

    private UUID teamId;
    private UUID pluginId;
    private PermissionLevel permissionLevel;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamPluginPermission setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public TeamPluginPermission setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public TeamPluginPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamPluginPermission that = (TeamPluginPermission) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(pluginId, that.pluginId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {

        return Objects.hash(teamId, pluginId, permissionLevel);
    }

    @Override
    public String toString() {
        return "TeamPluginPermission{" +
                "teamId=" + teamId +
                ", pluginId=" + pluginId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
