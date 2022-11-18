package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;

/**
 * A single-use temporary credential that can be used for processes such as validation or password resets.
 *
 */
public class CredentialTemporary {

    /**
     * The different types of temporary credentials.
     */
    public enum Type {
        //
        VALIDATION(TimeUnit.DAYS.toSeconds(7)),
        PASSWORD_RESET(TimeUnit.DAYS.toSeconds(1));

        // TTLs are expressed in seconds.
        long ttl;

        Type(long ttl) {
            this.ttl = ttl;
        }

        public long TTL() {
            return ttl;
        }
    }

    //
    private String authorizationCode;
    private Type type;
    private UUID accountId;

    public CredentialTemporary() {
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public CredentialTemporary setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
        return this;
    }

    public Type getType() {
        return type;
    }

    public CredentialTemporary setType(Type type) {
        this.type = type;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public CredentialTemporary setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CredentialTemporary that = (CredentialTemporary) o;
        return Objects.equals(authorizationCode, that.authorizationCode) && type == that.type && Objects.equals(
                accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorizationCode, type, accountId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("authorizationCode", authorizationCode)
                .add("type", type)
                .add("accountId", accountId)
                .toString();
    }
}
