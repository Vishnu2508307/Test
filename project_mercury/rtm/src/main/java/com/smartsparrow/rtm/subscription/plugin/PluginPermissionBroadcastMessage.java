package com.smartsparrow.rtm.subscription.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class PluginPermissionBroadcastMessage  implements BroadcastMessage {

    private static final long serialVersionUID = 1565041922199131976L;
    private final UUID pluginId;
    private final UUID accountId;
    private final UUID teamId;

    public PluginPermissionBroadcastMessage(final UUID pluginId, final UUID accountId, final UUID teamId) {
        this.pluginId = pluginId;
        this.accountId = accountId;
        this.teamId = teamId;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPermissionBroadcastMessage that = (PluginPermissionBroadcastMessage) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, accountId, teamId);
    }

    @Override
    public String toString() {
        return "PluginPermissionBroadcastMessage{" +
                "pluginId=" + pluginId +
                ", accountId=" + accountId +
                ", teamId=" + teamId +
                '}';
    }
}
