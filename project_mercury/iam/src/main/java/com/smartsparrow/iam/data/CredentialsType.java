package com.smartsparrow.iam.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.AuthenticationType;

public class CredentialsType {
    private UUID accountId;
    private String hash;
    private AuthenticationType authenticationType;

    public UUID getAccountId() {
        return accountId;
    }

    public CredentialsType setAccountId(final UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public CredentialsType setHash(final String hash) {
        this.hash = hash;
        return this;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public CredentialsType setAuthenticationType(final AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialsType that = (CredentialsType) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(hash, that.hash) &&
                authenticationType == that.authenticationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, hash, authenticationType);
    }

    @Override
    public String toString() {
        return "CredentialsType{" +
                "accountId=" + accountId +
                ", hash=" + hash +
                ", authenticationType=" + authenticationType +
                '}';
    }
}
