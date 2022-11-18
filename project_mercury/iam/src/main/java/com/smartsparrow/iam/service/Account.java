package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.MoreObjects;

import io.leangen.graphql.annotations.GraphQLIgnore;

/**
 * Account object providing basic, non personally identifiable information.
 */
public class Account {

    // the account id.
    private UUID id;
    // the historical user id (from MySQL)
    private Long historicalId;
    // the subscription id
    private UUID subscriptionId;
    // the data region in which this account keeps additional information.
    private Region iamRegion;
    // the status of the account
    private AccountStatus status;
    // the roles of the account
    private Set<AccountRole> roles;

    // FIXME: password_hash and password_expired here temporarily until credentials are implemented.
    private String passwordHash;
    private Boolean passwordExpired;
    private String passwordTemporary;

    public Account() {
    }

    public UUID getId() {
        return id;
    }

    public Account setId(UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Usage of this field should be replaced with getId() where possible.
     *
     * @return
     */
    @GraphQLIgnore
    @Deprecated
    public Long getHistoricalId() {
        return historicalId;
    }

    @Deprecated
    public Account setHistoricalId(Long historicalId) {
        this.historicalId = historicalId;
        return this;
    }

    @GraphQLIgnore // exposed from the Subscription
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public Account setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public Region getIamRegion() {
        return iamRegion;
    }

    public Account setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Account setStatus(AccountStatus status) {
        this.status = status;
        return this;
    }

    public Set<AccountRole> getRoles() {
        return roles;
    }

    public Account setRoles(Set<AccountRole> roles) {
        this.roles = roles;
        return this;
    }

    @GraphQLIgnore // yea, no.
    public String getPasswordHash() {
        return passwordHash;
    }

    public Account setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public Boolean getPasswordExpired() {
        return passwordExpired;
    }

    public Account setPasswordExpired(Boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
        return this;
    }

    @GraphQLIgnore // yea, no.
    public String getPasswordTemporary() {
        return passwordTemporary;
    }

    public Account setPasswordTemporary(String passwordTemporary) {
        this.passwordTemporary = passwordTemporary;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) && Objects.equals(historicalId, account.historicalId) && Objects.equals(
                subscriptionId, account.subscriptionId) && iamRegion == account.iamRegion && status == account.status
                && Objects.equals(roles, account.roles) && Objects.equals(passwordHash, account.passwordHash) && Objects
                .equals(passwordExpired, account.passwordExpired) && Objects.equals(passwordTemporary,
                                                                                    account.passwordTemporary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, historicalId, subscriptionId, iamRegion, status, roles, passwordHash, passwordExpired,
                            passwordTemporary);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("historicalId", historicalId)
                .add("subscriptionId", subscriptionId)
                .add("iamRegion", iamRegion)
                .add("status", status)
                .add("roles", roles)
                .add("passwordExpired", passwordExpired)
                .toString();
    }
}
