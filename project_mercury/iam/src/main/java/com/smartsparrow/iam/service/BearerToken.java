package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

/**
 * Security token which must be sent by client to get access to the system.
 * Bearers token can be different types: permanent, temporary. This entity doesn't care about type.
 * It can be understood as "give access to the bearer of this token".
 */
public class BearerToken {

    private String token;
    private UUID accountId;

    public String getToken() {
        return token;
    }

    public BearerToken setToken(String token) {
        this.token = token;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public BearerToken setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public String toString() {
        return "BearerToken{" +
                "token='***" + (token != null ? token.substring(token.length() - 4) : null) + '\'' + //print last 4 symbols
                ", account_id=" + accountId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BearerToken that = (BearerToken) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(token, accountId);
    }
}
