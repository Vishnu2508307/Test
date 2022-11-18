package com.smartsparrow.iam.data.permission.subscription;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class AccountSubscriptionPermission {

    private UUID accountId;
    private UUID subscriptionId;
    private PermissionLevel permissionLevel;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountSubscriptionPermission setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public AccountSubscriptionPermission setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public AccountSubscriptionPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountSubscriptionPermission that = (AccountSubscriptionPermission) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, subscriptionId, permissionLevel);
    }

    @Override
    public String toString() {
        return "AccountSubscriptionPermission{" +
                "accountId=" + accountId +
                ", subscriptionId=" + subscriptionId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
