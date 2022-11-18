package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

public class BronteWebToken extends AbstractWebToken {

    private long createdTs;
    private long validUntilTs;
    private UUID authoritySubscriptionId;
    private UUID authorityRelyingPartyId;

    public BronteWebToken(String token) {
        super(token);
    }

    @Override
    public WebTokenType getWebTokenType() {
        return WebTokenType.BRONTE;
    }

    public long getCreatedTs() {
        return createdTs;
    }

    public BronteWebToken setCreatedTs(long createdTs) {
        this.createdTs = createdTs;
        return this;
    }

    @Override
    public long getValidUntilTs() {
        return validUntilTs;
    }

    public BronteWebToken setValidUntilTs(long validUntilTs) {
        this.validUntilTs = validUntilTs;
        return this;
    }

    @Nullable
    public UUID getAuthoritySubscriptionId() { return authoritySubscriptionId; }

    public BronteWebToken setAuthoritySubscriptionId(final UUID authoritySubscriptionId) {
        this.authoritySubscriptionId = authoritySubscriptionId;
        return this;
    }

    @Nullable
    public UUID getAuthorityRelyingPartyId() { return authorityRelyingPartyId; }

    public BronteWebToken setAuthorityRelyingPartyId(final UUID authorityRelyingPartyId) {
        this.authorityRelyingPartyId = authorityRelyingPartyId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BronteWebToken that = (BronteWebToken) o;
        return createdTs == that.createdTs && validUntilTs == that.validUntilTs && Objects.equals(
                authoritySubscriptionId,
                that.authoritySubscriptionId) && Objects.equals(authorityRelyingPartyId,
                                                                that.authorityRelyingPartyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                            createdTs,
                            validUntilTs,
                            authoritySubscriptionId,
                            authorityRelyingPartyId);
    }

    @Override
    public String toString() {
        return "BronteWebToken{" +
                "createdTs=" + createdTs +
                ", validUntilTs=" + validUntilTs +
                ", authoritySubscriptionId=" + authoritySubscriptionId +
                ", authorityRelyingPartyId=" + authorityRelyingPartyId +
                '}';
    }
}
