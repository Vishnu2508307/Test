package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

/**
 * An IAM Claim is a name/value pair for a user which is provided by a third party system.
 *
 * The subscription id used here is the subscription associated to the authentication which provided the claims
 *
 */
public class Claim {

    private UUID subscriptionId;
    private Region iamRegion;
    private UUID accountId;
    private String name;
    private String value;

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public Claim setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public Region getIamRegion() {
        return iamRegion;
    }

    public Claim setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Claim setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Claim setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Claim setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Claim claim = (Claim) o;
        return Objects.equals(subscriptionId, claim.subscriptionId) && iamRegion == claim.iamRegion
                && Objects.equals(accountId, claim.accountId) && Objects.equals(name, claim.name)
                && Objects.equals(value, claim.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, iamRegion, accountId, name, value);
    }

    @Override
    public String toString() {
        return "Claim{" + "subscriptionId=" + subscriptionId + ", iamRegion=" + iamRegion + ", accountId=" + accountId
                + ", name='" + name + '\'' + ", value='" + value + '\'' + '}';
    }
}
