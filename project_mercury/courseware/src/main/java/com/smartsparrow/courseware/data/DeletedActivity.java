package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class DeletedActivity {
    private UUID activityId;
    private UUID accountId;
    private String deletedAt;

    public UUID getActivityId() {
        return activityId;
    }

    public DeletedActivity setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public DeletedActivity setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public DeletedActivity setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletedActivity that = (DeletedActivity) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(deletedAt, that.deletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, accountId, deletedAt);
    }

    @Override
    public String toString() {
        return "DeletedActivity{" +
                "workspaceId=" + activityId +
                ", accountId=" + accountId +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
