package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

public class AccountAccess {

    private UUID accountId;
    private Boolean aeroAccess;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountAccess setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public Boolean getAeroAccess() {
        return aeroAccess;
    }

    public AccountAccess setAeroAccess(Boolean aeroAccess) {
        this.aeroAccess = aeroAccess;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountAccess that = (AccountAccess) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(aeroAccess, that.aeroAccess);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, aeroAccess);
    }
}
