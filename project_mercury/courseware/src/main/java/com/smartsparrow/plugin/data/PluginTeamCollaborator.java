package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class PluginTeamCollaborator extends PluginCollaborator {

    private UUID teamId;

    public UUID getTeamId() {
        return teamId;
    }

    public PluginTeamCollaborator setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public PluginTeamCollaborator setPluginId(UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public PluginTeamCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PluginTeamCollaborator that = (PluginTeamCollaborator) o;
        return Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamId);
    }

    @Override
    public String toString() {
        return "PluginTeamCollaborator{" +
                "teamId=" + teamId +
                "} " + super.toString();
    }
}
