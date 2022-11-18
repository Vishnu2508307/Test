package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.MoreObjects;

import io.leangen.graphql.annotations.GraphQLIgnore;

/**
 * Identity Attributes for an Account, contains personally identifiable information.
 */
public class AccountIdentityAttributes {

    private UUID accountId;
    private UUID subscriptionId;
    private Region iamRegion;

    // these name fields are based on: https://www.w3.org/International/questions/qa-personal-names
    private String honorificPrefix;
    private String givenName;
    private String familyName;
    private String honorificSuffix;

    // validated emails only.
    private Set<String> email;
    private String primaryEmail;

    // the user's supplied university or institution
    private String affiliation;
    //
    private String jobTitle;

    public AccountIdentityAttributes() {
    }

    @GraphQLIgnore // exposed as part of the account.
    public UUID getAccountId() {
        return accountId;
    }

    public AccountIdentityAttributes setAccountId(UUID id) {
        this.accountId = id;
        return this;
    }

    @GraphQLIgnore // exposed as part of the account.
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public AccountIdentityAttributes setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    @GraphQLIgnore // exposed as part of the account.
    public Region getIamRegion() {
        return iamRegion;
    }

    public AccountIdentityAttributes setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public AccountIdentityAttributes setHonorificPrefix(String honorificPrefix) {
        this.honorificPrefix = honorificPrefix;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public AccountIdentityAttributes setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public AccountIdentityAttributes setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public AccountIdentityAttributes setHonorificSuffix(String honorificSuffix) {
        this.honorificSuffix = honorificSuffix;
        return this;
    }

    public Set<String> getEmail() {
        return email;
    }

    public AccountIdentityAttributes setEmail(Set<String> email) {
        this.email = email;
        return this;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public AccountIdentityAttributes setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
        return this;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public AccountIdentityAttributes setAffiliation(String affiliation) {
        this.affiliation = affiliation;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public AccountIdentityAttributes setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountIdentityAttributes that = (AccountIdentityAttributes) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(subscriptionId, that.subscriptionId)
                && iamRegion == that.iamRegion && Objects.equals(honorificPrefix, that.honorificPrefix) && Objects
                .equals(givenName, that.givenName) && Objects.equals(familyName, that.familyName) && Objects
                .equals(honorificSuffix, that.honorificSuffix) && Objects.equals(email, that.email) && Objects
                .equals(primaryEmail, that.primaryEmail) && Objects.equals(affiliation, that.affiliation) && Objects
                .equals(jobTitle, that.jobTitle);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(accountId, subscriptionId, iamRegion, honorificPrefix, givenName, familyName, honorificSuffix,
                      email, primaryEmail, affiliation, jobTitle);
    }

    /**
     * Only output specific fields due to PII being logged; other use cases should output the specific fields required.
     *
     * @return a string with specific fields.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", accountId).add("subscriptionId", subscriptionId).toString();
    }
}
