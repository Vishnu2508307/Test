package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

public class DeveloperKey {

    private String key;
    private UUID subscriptionId;
    private UUID accountId;
    private long createdTs;

    public String getKey() {
        return key;
    }

    public DeveloperKey setKey(String key) {
        this.key = key;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public DeveloperKey setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public DeveloperKey setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public long getCreatedTs() {
        return createdTs;
    }

    public DeveloperKey setCreatedTs(long createdTs) {
        this.createdTs = createdTs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeveloperKey that = (DeveloperKey) o;
        return createdTs == that.createdTs &&
                Objects.equals(key, that.key) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, subscriptionId, accountId, createdTs);
    }
}
