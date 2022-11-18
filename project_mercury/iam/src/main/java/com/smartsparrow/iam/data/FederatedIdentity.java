package com.smartsparrow.iam.data;

import java.util.Objects;
import java.util.UUID;

public class FederatedIdentity {

    private UUID subscriptionId;
    private String clientId;
    private String subjectId;
    private UUID accountId;

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public FederatedIdentity setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public FederatedIdentity setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public FederatedIdentity setSubjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public FederatedIdentity setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FederatedIdentity that = (FederatedIdentity) o;
        return Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(subjectId, that.subjectId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, clientId, subjectId, accountId);
    }

    @Override
    public String toString() {
        return "FederatedIdentity{" +
                "subscriptionId=" + subscriptionId +
                ", clientId='" + clientId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", accountId=" + accountId +
                '}';
    }
}
