package com.smartsparrow.iam.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Standard Account information plus additional attributes, depending on how it was hydrated.
 *
 * Note: This is not intended to be used in Mutations, only a convenience to optimize reads!
 *
 */
public class AccountAdapter {

    private Account account;

    private AccountIdentityAttributes identityAttributes;

    private List<AccountAction> actions;

    private List<AccountShadowAttribute> shadowAttributes;

    private List<AccountAvatar> avatars;

    private Map<AccountShadowAttributeName, AccountShadowAttribute> legacyAttributes;

    private List<AccountLogEntry> legacyLogActions;

    public AccountAdapter() {
    }

    public Account getAccount() {
        return account;
    }

    public AccountAdapter setAccount(Account account) {
        this.account = account;
        return this;
    }

    public AccountIdentityAttributes getIdentityAttributes() {
        return identityAttributes;
    }

    public AccountAdapter setIdentityAttributes(AccountIdentityAttributes identityAttributes) {
        this.identityAttributes = identityAttributes;
        return this;
    }

    public List<AccountAction> getActions() {
        return actions;
    }

    public AccountAdapter setActions(List<AccountAction> actions) {
        this.actions = actions;
        return this;
    }

    public List<AccountShadowAttribute> getShadowAttributes() {
        return shadowAttributes;
    }

    public AccountAdapter setShadowAttributes(List<AccountShadowAttribute> shadowAttributes) {
        this.shadowAttributes = shadowAttributes;
        return this;
    }

    public List<AccountAvatar> getAvatars() {
        return avatars;
    }

    public AccountAdapter setAvatars(List<AccountAvatar> avatars) {
        this.avatars = avatars;
        return this;
    }

    @Deprecated
    public Map<AccountShadowAttributeName, AccountShadowAttribute> getLegacyAttributes() {
        return legacyAttributes;
    }

    public AccountAdapter setLegacyAttributes(Map<AccountShadowAttributeName, AccountShadowAttribute> legacyAttributes) {
        this.legacyAttributes = legacyAttributes;
        return this;
    }

    @Deprecated
    public List<AccountLogEntry> getLegacyLogActions() {
        return legacyLogActions;
    }

    public AccountAdapter setLegacyLogActions(List<AccountLogEntry> legacyLogActions) {
        this.legacyLogActions = legacyLogActions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountAdapter adapter = (AccountAdapter) o;
        return Objects.equals(account, adapter.account) && Objects.equals(identityAttributes,
                                                                          adapter.identityAttributes) && Objects.equals(
                actions, adapter.actions) && Objects.equals(shadowAttributes, adapter.shadowAttributes)
                && Objects.equals(avatars, adapter.avatars) && Objects.equals(legacyAttributes,
                                                                              adapter.legacyAttributes)
                && Objects.equals(legacyLogActions, adapter.legacyLogActions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, identityAttributes, actions, shadowAttributes, avatars, legacyAttributes,
                            legacyLogActions);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("account", account)
                .add("identityAttributes", identityAttributes)
                .add("actions", actions)
                .add("shadowAttributes", shadowAttributes)
                .add("avatars", avatars)
                .add("legacyAttributes", legacyAttributes)
                .add("legacyLogActions", legacyLogActions)
                .toString();
    }
}
