package com.smartsparrow.iam.data.permission.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class AccountCohortPermission {

    private UUID accountId;
    private UUID cohortId;
    private PermissionLevel permissionLevel;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountCohortPermission setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public AccountCohortPermission setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public AccountCohortPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountCohortPermission that = (AccountCohortPermission) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(cohortId, that.cohortId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, cohortId, permissionLevel);
    }

    @Override
    public String toString() {
        return "AccountCohortPermission{" +
                "accountId=" + accountId +
                ", cohortId=" + cohortId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
