package com.smartsparrow.iam.payload;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.iam.service.AuthenticationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.Region;

/**
 * This class is aimed to represent user information in a RTM response
 */
public class AccountPayload {

    private static final Logger logger = LoggerFactory.getLogger(AccountPayload.class);

    private UUID accountId;
    private UUID subscriptionId;
    private Region iamRegion;
    private String honorificPrefix;
    private String givenName;
    private String familyName;
    private String honorificSuffix;
    private String primaryEmail;
    private Set<AccountRole> roles;
    private String avatarSmall;
    private Set<String> email;
    private String affiliation;
    private String jobTitle;
    private AuthenticationType authenticationType;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountPayload setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public AccountPayload setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public Region getIamRegion() {
        return iamRegion;
    }

    public AccountPayload setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public AccountPayload setHonorificPrefix(String honorificPrefix) {
        this.honorificPrefix = honorificPrefix;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public AccountPayload setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public AccountPayload setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public AccountPayload setHonorificSuffix(String honorificSuffix) {
        this.honorificSuffix = honorificSuffix;
        return this;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public AccountPayload setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
        return this;
    }

    public Set<AccountRole> getRoles() {
        return roles;
    }

    public AccountPayload setRoles(Set<AccountRole> roles) {
        this.roles = roles;
        return this;
    }

    public String getAvatarSmall() {
        return avatarSmall;
    }

    public AccountPayload setAvatarSmall(String avatarSmall) {
        this.avatarSmall = avatarSmall;
        return this;
    }

    public Set<String> getEmail() {
        return email;
    }

    public AccountPayload setEmail(Set<String> email) {
        this.email = email;
        return this;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public AccountPayload setAffiliation(String affiliation) {
        this.affiliation = affiliation;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public AccountPayload setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public AccountPayload setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
        return this;
    }

    /**
     * Helper method to create {@link AccountPayload}
     * @param account account
     * @param identity identity attributes, can be empty object if for some reason identity not found
     * @param avatar avatar, if account doesn't have avatar, should be empty object
     * @return account payload
     */
    public static AccountPayload from(@Nonnull Account account, @Nonnull AccountIdentityAttributes identity,
                                      @Nonnull AccountAvatar avatar, @Nonnull AuthenticationType authType) {
        if (identity.getPrimaryEmail() == null) {
            logger.warn("Something goes wrong: identity for account {} not found", account.getId());
        }
        AccountPayload payload = new AccountPayload()
                .setAccountId(account.getId())
                .setSubscriptionId(account.getSubscriptionId())
                .setIamRegion(account.getIamRegion())
                .setGivenName(identity.getGivenName())
                .setFamilyName(identity.getFamilyName())
                .setPrimaryEmail(identity.getPrimaryEmail())
                .setEmail(identity.getEmail())
                .setRoles(account.getRoles())
                .setHonorificPrefix(identity.getHonorificPrefix())
                .setHonorificSuffix(identity.getHonorificSuffix())
                .setAffiliation(identity.getAffiliation())
                .setJobTitle(identity.getJobTitle())
                .setAuthenticationType(authType);

        String data = avatar.getData();
        String mime = avatar.getMimeType();

        // do not return the avatarSmall if this is null
        if (data != null && mime != null) {
            payload.setAvatarSmall(String.format("data:%s;base64,%s", mime, data));
        }
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountPayload that = (AccountPayload) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                iamRegion == that.iamRegion &&
                Objects.equals(honorificPrefix, that.honorificPrefix) &&
                Objects.equals(givenName, that.givenName) &&
                Objects.equals(familyName, that.familyName) &&
                Objects.equals(honorificSuffix, that.honorificSuffix) &&
                Objects.equals(primaryEmail, that.primaryEmail) &&
                Objects.equals(roles, that.roles) &&
                Objects.equals(avatarSmall, that.avatarSmall) &&
                Objects.equals(email, that.email) &&
                Objects.equals(affiliation, that.affiliation) &&
                Objects.equals(jobTitle, that.jobTitle);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, subscriptionId, iamRegion, honorificPrefix, givenName, familyName,
                honorificSuffix, primaryEmail, roles, avatarSmall, email, affiliation, jobTitle);
    }

    @Override
    public String toString() {
        return "AccountPayload{" +
                "accountId=" + accountId +
                ", subscriptionId=" + subscriptionId +
                ", iamRegion=" + iamRegion +
                ", honorificPrefix='" + honorificPrefix + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", honorificSuffix='" + honorificSuffix + '\'' +
                ", primaryEmail='" + primaryEmail + '\'' +
                ", roles=" + roles +
                ", avatarSmall='" + avatarSmall + '\'' +
                ", email=" + email +
                ", affiliation='" + affiliation + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                '}';
    }
}
