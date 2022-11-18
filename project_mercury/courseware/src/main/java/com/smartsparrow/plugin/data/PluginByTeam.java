package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

/**
    This represents plugin visibility (accessibility) for a team in workspace context
 */
public class PluginByTeam {

    private UUID teamId;
    private UUID pluginId;

    public UUID getTeamId() {
        return teamId;
    }

    public PluginByTeam setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public PluginByTeam setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginByTeam that = (PluginByTeam) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(pluginId, that.pluginId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, pluginId);
    }

    @Override
    public String toString() {
        return "PluginByTeam{" +
                "teamId=" + teamId +
                ", pluginId=" + pluginId +
                '}';
    }
}
