package com.smartsparrow.publication.data;

import java.util.Objects;
import java.util.UUID;

public class ActivityByAccount {

    private UUID accountId;
    private UUID activityId;

    public UUID getAccountId() {
        return accountId;
    }

    public ActivityByAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public ActivityByAccount setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityByAccount that = (ActivityByAccount) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, activityId);
    }

    @Override
    public String toString() {
        return "ActivityByAccount{" +
                "accountId=" + accountId +
                ", activityId=" + activityId +
                '}';
    }
}
