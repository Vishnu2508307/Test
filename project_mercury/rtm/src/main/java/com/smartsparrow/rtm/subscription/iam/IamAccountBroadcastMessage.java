package com.smartsparrow.rtm.subscription.iam;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class IamAccountBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -5515631302287755502L;
    public final UUID accountId;
    public final UUID accountSubscriptionId;

    public IamAccountBroadcastMessage(UUID accountSubscriptionId, UUID accountId) {

        this.accountId = accountId;
        this.accountSubscriptionId = accountSubscriptionId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getAccountSubscriptionId() {
        return accountSubscriptionId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IamAccountBroadcastMessage that = (IamAccountBroadcastMessage) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(accountSubscriptionId, that.accountSubscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, accountSubscriptionId);
    }

    @Override
    public String toString() {
        return "IamAccountBroadcastMessage{" +
                "accountId=" + accountId +
                ", accountSubscriptionId=" + accountSubscriptionId +
                '}';
    }
}
