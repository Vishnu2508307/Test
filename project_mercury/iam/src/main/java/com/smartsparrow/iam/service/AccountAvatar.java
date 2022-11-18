package com.smartsparrow.iam.service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.google.common.base.MoreObjects;

import io.leangen.graphql.annotations.GraphQLIgnore;

/**
 * The avatar for the account. These objects are stored as "named" data, e.g. "ORIGINAL" or "SMALL"
 *
 */
public class AccountAvatar {

    public enum Size {
        SMALL,
        LARGE,
        MEDIUM,
        ORIGINAL;
    }

    // the account identifier
    private UUID accountId;

    // the iam data region
    private Region iamRegion;

    // the name of the avatar, currently the "size"
    private Size name;

    // the mimetype of the underlying data.
    private String mimeType;

    // base64 encoded binary data.
    private String data;

    // name type specific information, such as dimensions.
    private Map<String, String> meta;

    public AccountAvatar() {
    }

    @GraphQLIgnore
    public UUID getAccountId() {
        return accountId;
    }

    public AccountAvatar setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @GraphQLIgnore
    public Region getIamRegion() {
        return iamRegion;
    }

    public AccountAvatar setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    public Size getName() {
        return name;
    }

    public AccountAvatar setName(Size name) {
        this.name = name;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public AccountAvatar setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getData() {
        return data;
    }

    public AccountAvatar setData(String data) {
        this.data = data;
        return this;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public AccountAvatar setMeta(Map<String, String> meta) {
        this.meta = meta;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountAvatar that = (AccountAvatar) o;
        return Objects.equals(accountId, that.accountId) && iamRegion == that.iamRegion && name == that.name && Objects
                .equals(mimeType, that.mimeType) && Objects.equals(data, that.data) && Objects.equals(meta, that.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, iamRegion, name, mimeType, data, meta);
    }

    /*
     * Did not include data field
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("accountId", accountId).add("name", name).add("mimeType", mimeType)
                .add("meta", meta).toString();
    }
}
