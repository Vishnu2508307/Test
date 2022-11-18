package com.smartsparrow.iam.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;

public class AccountSummaryPayload {

    private UUID accountId;
    private UUID subscriptionId;
    private String givenName;
    private String familyName;
    private String primaryEmail;
    private String avatarSmall;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountSummaryPayload setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public AccountSummaryPayload setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public AccountSummaryPayload setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public AccountSummaryPayload setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public AccountSummaryPayload setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
        return this;
    }

    public String getAvatarSmall() {
        return avatarSmall;
    }

    public AccountSummaryPayload setAvatarSmall(String avatarSmall) {
        this.avatarSmall = avatarSmall;
        return this;
    }

    /**
     * Helper method to create an account summary payload
     *
     * @param identity      the identity attribute
     * @param accountAvatar this is an empty object when the avatar is not defined
     * @return a {@link AccountSummaryPayload}
     */
    public static AccountSummaryPayload from(@Nonnull AccountIdentityAttributes identity,
                                             @Nonnull AccountAvatar accountAvatar) {

        AccountSummaryPayload payload = new AccountSummaryPayload()
                .setAccountId(identity.getAccountId())
                .setSubscriptionId(identity.getSubscriptionId())
                .setGivenName(identity.getGivenName())
                .setFamilyName(identity.getFamilyName())
                .setPrimaryEmail(identity.getPrimaryEmail());

        String data = accountAvatar.getData();
        String mime = accountAvatar.getMimeType();

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
        AccountSummaryPayload that = (AccountSummaryPayload) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(givenName, that.givenName) &&
                Objects.equals(familyName, that.familyName) &&
                Objects.equals(primaryEmail, that.primaryEmail) &&
                Objects.equals(avatarSmall, that.avatarSmall);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, subscriptionId, givenName, familyName, primaryEmail, avatarSmall);
    }

    @Override
    public String toString() {
        return "AccountSummaryPayload{" +
                "accountId=" + accountId +
                ", subscriptionId=" + subscriptionId +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", primaryEmail='" + primaryEmail + '\'' +
                ", avatarSmall='" + avatarSmall + '\'' +
                '}';
    }
}
