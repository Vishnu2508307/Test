package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

/**
 * Describes an LTI launch an account performed over a cohort. This tracking is necessary so the enrollment can
 * be performed when a valid IES pearsonId exists. The id is required to return PII from IES during cohort
 * enrollment listing
 */
public class AccountCohortLTILaunch {

    private UUID accountId;
    private UUID cohortId;
    private Integer ttlSeconds;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountCohortLTILaunch setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public AccountCohortLTILaunch setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public Integer getTtlSeconds() {
        return ttlSeconds;
    }

    public AccountCohortLTILaunch setTtlSeconds(Integer ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountCohortLTILaunch that = (AccountCohortLTILaunch) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(ttlSeconds, that.ttlSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, cohortId, ttlSeconds);
    }

    @Override
    public String toString() {
        return "AccountCohortLTILaunch{" +
                "accountId=" + accountId +
                ", cohortId=" + cohortId +
                ", ttlSeconds=" + ttlSeconds +
                '}';
    }
}
