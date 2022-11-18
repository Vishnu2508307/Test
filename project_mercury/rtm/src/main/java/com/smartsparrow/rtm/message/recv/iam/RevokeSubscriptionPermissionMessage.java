package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "message currently not used")
public class RevokeSubscriptionPermissionMessage extends ReceivedMessage implements SubscriptionMessage {

    private UUID subscriptionId;
    private UUID teamId;
    private UUID accountId;

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevokeSubscriptionPermissionMessage that = (RevokeSubscriptionPermissionMessage) o;
        return Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(teamId, that.teamId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, teamId, accountId);
    }

    @Override
    public String toString() {
        return "RevokeSubscriptionPermissionMessage{" +
                "subscriptionId=" + subscriptionId +
                ", teamId=" + teamId +
                ", accountId=" + accountId +
                '}';
    }
}
