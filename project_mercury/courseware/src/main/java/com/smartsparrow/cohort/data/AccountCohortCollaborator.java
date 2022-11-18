package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class AccountCohortCollaborator extends CohortCollaborator {

    private UUID accountId;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountCohortCollaborator setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public AccountCohortCollaborator setCohortId(UUID cohortId) {
        super.setCohortId(cohortId);
        return this;
    }

    @Override
    public AccountCohortCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccountCohortCollaborator that = (AccountCohortCollaborator) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountId);
    }

    @Override
    public String toString() {
        return "AccountCohortCollaborator{" +
                "accountId=" + accountId +
                "} " + super.toString();
    }
}
