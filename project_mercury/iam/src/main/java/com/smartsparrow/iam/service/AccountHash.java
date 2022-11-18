package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

import com.google.common.base.MoreObjects;

public class AccountHash {

    // a one way hash.
    private String hash;

    // the account id
    private UUID accountId;

    // the region of the account
    private Region iamRegion;

    public AccountHash() {
    }

    public String getHash() {
        return hash;
    }

    public AccountHash setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public AccountHash setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public Region getIamRegion() {
        return iamRegion;
    }

    public AccountHash setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountHash that = (AccountHash) o;
        return Objects.equals(hash, that.hash) && Objects.equals(accountId, that.accountId)
                && iamRegion == that.iamRegion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, accountId, iamRegion);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hash", hash).add("accountId", accountId)
                .add("iamRegion", iamRegion).toString();
    }
}
