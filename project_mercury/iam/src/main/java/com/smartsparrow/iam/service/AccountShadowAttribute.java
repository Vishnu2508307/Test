package com.smartsparrow.iam.service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.google.common.base.MoreObjects;

/**
 * Account attributes which are generally derived; these are generally not displayed to users.
 *
 * These are considered PII and should be treated as such.
 *
 * They are stored as key/value pairs, with a time based uuid and source every time that the attribute is derived. This
 * provides a faux-confidence for attributes which are recorded multiple times.
 */
public class AccountShadowAttribute {

    private UUID accountId;
    private Region iamRegion;

    // what to track, e.g. 'attending university'
    private AccountShadowAttributeName attribute;

    // what this information is, e.g. 'Starfleet Academy'
    private String value;

    // how and when this information was gathered, e.g. now() : 'lti'
    private Map<UUID, AccountShadowAttributeSource> source;

    public AccountShadowAttribute() {
    }

    public UUID getAccountId() {
        return accountId;
    }

    public AccountShadowAttribute setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public Region getIamRegion() {
        return iamRegion;
    }

    public AccountShadowAttribute setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    public AccountShadowAttributeName getAttribute() {
        return attribute;
    }

    public AccountShadowAttribute setAttribute(AccountShadowAttributeName attribute) {
        this.attribute = attribute;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AccountShadowAttribute setValue(String value) {
        this.value = value;
        return this;
    }

    public Map<UUID, AccountShadowAttributeSource> getSource() {
        return source;
    }

    public AccountShadowAttribute setSource(Map<UUID, AccountShadowAttributeSource> source) {
        this.source = source;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountShadowAttribute that = (AccountShadowAttribute) o;
        return Objects.equals(accountId, that.accountId) && iamRegion == that.iamRegion && attribute == that.attribute
                && Objects.equals(value, that.value) && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, iamRegion, attribute, value, source);
    }

    /**
     * Only output specific fields due to PII being logged; other use cases should output the specific fields required.
     *
     * @return a string with specific fields.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("accountId", accountId).add("attribute", attribute).toString();
    }
}
