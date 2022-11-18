package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PluginPermissionMessage extends ReceivedMessage implements PluginMessage {

    private UUID accountId;
    private UUID teamId;
    private UUID pluginId;
    private PermissionLevel permissionLevel;

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPermissionMessage that = (PluginPermissionMessage) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(teamId, that.teamId) &&
                Objects.equals(pluginId, that.pluginId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, teamId, pluginId, permissionLevel);
    }

    @Override
    public String toString() {
        return "PluginPermissionMessage{" +
                "accountId=" + accountId +
                ", teamId=" + teamId +
                ", pluginId=" + pluginId +
                ", permissionLevel=" + permissionLevel +
                "} " + super.toString();
    }
}
