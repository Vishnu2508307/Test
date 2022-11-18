package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Security token generated when user is authenticated via http or web socket.
 *
 */
@Deprecated
public class WebSessionToken extends BearerToken {

    private long createdTs;
    private long validUntilTs;
    private UUID authoritySubscriptionId;
    private UUID authorityRelyingPartyId;

    /**
     *
     * @return a timestamp of when the token was created.
     */
    public long getCreatedTs() {
        return createdTs;
    }

    public WebSessionToken setCreatedTs(long createdTs) {
        this.createdTs = createdTs;
        return this;
    }

    /**
     *
     * @return a timestamp of when this token will expire
     */
    public long getValidUntilTs() {
        return validUntilTs;
    }

    public WebSessionToken setValidUntilTs(long validUntilTs) {
        this.validUntilTs = validUntilTs;
        return this;
    }

    /**
     * Set the token value
     * @param token the token value
     * @return current object for chaining.
     */
    @Override
    public WebSessionToken setToken(String token) {
        super.setToken(token);
        return this;
    }

    /**
     * Set the account id related to this token
     * @param accountId the account id
     * @return current object for chaining.
     */
    @Override
    public WebSessionToken setAccountId(UUID accountId) {
        super.setAccountId(accountId);
        return this;
    }

    /**
     *
     * @return the subscription on which this authentication was performed, can be null.
     */
    @Nullable
    public UUID getAuthoritySubscriptionId() {
        return authoritySubscriptionId;
    }

    public WebSessionToken setAuthoritySubscriptionId(@Nullable UUID authoritySubscriptionId) {
        this.authoritySubscriptionId = authoritySubscriptionId;
        return this;
    }

    /**
     *
     * @return the authentication mechanism which was used in order to perform the authentication.
     */
    @Nullable
    public UUID getAuthorityRelyingPartyId() {
        return authorityRelyingPartyId;
    }

    public WebSessionToken setAuthorityRelyingPartyId(@Nullable UUID authorityRelyingPartyId) {
        this.authorityRelyingPartyId = authorityRelyingPartyId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        WebSessionToken that = (WebSessionToken) o;
        return createdTs == that.createdTs && validUntilTs == that.validUntilTs && Objects.equals(
                authorityRelyingPartyId, that.authorityRelyingPartyId) && Objects.equals(authoritySubscriptionId,
                                                                                         that.authoritySubscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), createdTs, validUntilTs, authorityRelyingPartyId,
                            authoritySubscriptionId);
    }

    @Override
    public String toString() {
        return "WebSessionToken{" + "createdTs=" + createdTs + ", validUntilTs=" + validUntilTs
                + ", authorityRelyingPartyId=" + authorityRelyingPartyId + ", authoritySubscriptionId="
                + authoritySubscriptionId + "} " + super.toString();
    }

}
