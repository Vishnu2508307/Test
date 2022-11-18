package com.smartsparrow.sso.service;

import java.util.UUID;

import com.google.common.base.Objects;

public class SessionAccount {

    private UUID id;
    private UUID accountId;
    private String sessionId;

    public SessionAccount() {
    }

    public UUID getId() {
        return id;
    }

    public SessionAccount setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public SessionAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public SessionAccount setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SessionAccount that = (SessionAccount) o;
        return Objects.equal(id, that.id) && Objects.equal(accountId, that.accountId) && Objects.equal(sessionId,
                                                                                                       that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, accountId, sessionId);
    }

    @Override
    public String toString() {
        return "SessionAccount{" + "id=" + id + ", accountId=" + accountId + ", sessionId='" + sessionId + '\'' + '}';
    }
}
