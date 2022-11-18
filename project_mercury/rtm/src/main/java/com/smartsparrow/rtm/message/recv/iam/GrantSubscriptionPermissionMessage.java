package com.smartsparrow.rtm.message.recv.iam;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GrantSubscriptionPermissionMessage extends ReceivedMessage implements SubscriptionMessage {

    private List<UUID> accountIds;
    private List<UUID> teamIds;
    private UUID subscriptionId;
    private PermissionLevel permissionLevel;

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public List<UUID> getAccountIds() {
        return accountIds;
    }

    public List<UUID> getTeamIds() {
        return teamIds;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantSubscriptionPermissionMessage that = (GrantSubscriptionPermissionMessage) o;
        return Objects.equals(accountIds, that.accountIds) &&
                Objects.equals(teamIds, that.teamIds) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountIds, teamIds, subscriptionId, permissionLevel);
    }

    @Override
    public String toString() {
        return "GrantSubscriptionPermissionMessage{" +
                "accountIds=" + accountIds +
                ", teamIds=" + teamIds +
                ", subscriptionId=" + subscriptionId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
