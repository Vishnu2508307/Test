package com.smartsparrow.iam.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class SubscriptionAccountCollaborator extends SubscriptionCollaborator {

    private UUID accountId;

    public UUID getAccountId() {
        return accountId;
    }

    public SubscriptionAccountCollaborator setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public SubscriptionAccountCollaborator setSubscriptionId(UUID subscriptionId) {
        super.setSubscriptionId(subscriptionId);
        return this;
    }

    @Override
    public SubscriptionAccountCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubscriptionAccountCollaborator that = (SubscriptionAccountCollaborator) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountId);
    }

    @Override
    public String toString() {
        return "SubscriptionAccountCollaborator{" +
                "accountId=" + accountId +
                "} " + super.toString();
    }
}
