package com.smartsparrow.iam.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public abstract class SubscriptionCollaborator {

    private UUID subscriptionId;
    private PermissionLevel permissionLevel;

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public SubscriptionCollaborator setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public SubscriptionCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionCollaborator that = (SubscriptionCollaborator) o;
        return Objects.equals(subscriptionId, that.subscriptionId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, permissionLevel);
    }

    @Override
    public String toString() {
        return "SubscriptionCollaborator{" +
                "subscriptionId=" + subscriptionId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
