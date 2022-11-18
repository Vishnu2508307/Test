package com.smartsparrow.iam.data;

import java.util.Objects;
import java.util.UUID;

public class IESAccountTracking {

    private UUID accountId;
    private String iesUserId;

    public UUID getAccountId() {
        return accountId;
    }

    public IESAccountTracking setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getIesUserId() {
        return iesUserId;
    }

    public IESAccountTracking setIesUserId(String iesUserId) {
        this.iesUserId = iesUserId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IESAccountTracking that = (IESAccountTracking) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(iesUserId, that.iesUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, iesUserId);
    }

    @Override
    public String toString() {
        return "IESAccount{" +
                "accountId=" + accountId +
                ", iesUserId='" + iesUserId + '\'' +
                '}';
    }
}
