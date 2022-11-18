package com.smartsparrow.iam.data;

import java.util.Objects;
import java.util.UUID;

public class SubscriptionAccount {

    private UUID subscriptionId;
    private UUID accountId;

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public SubscriptionAccount setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public SubscriptionAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionAccount that = (SubscriptionAccount) o;
        return Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, accountId);
    }

    @Override
    public String toString() {
        return "SubscriptionAccount{" +
                "subscriptionId=" + subscriptionId +
                ", accountId=" + accountId +
                '}';
    }
}
