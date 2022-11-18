package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

public class CohortAccount {

    private UUID cohortId;
    private UUID accountId;

    public UUID getCohortId() {
        return cohortId;
    }

    public CohortAccount setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public CohortAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortAccount that = (CohortAccount) o;
        return Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortId, accountId);
    }

    @Override
    public String toString() {
        return "CohortAccount{" +
                "cohortId=" + cohortId +
                ", accountId=" + accountId +
                '}';
    }
}
