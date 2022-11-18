package com.smartsparrow.sso.service;

import java.util.Objects;

import com.smartsparrow.iam.service.AbstractWebToken;
import com.smartsparrow.iam.service.WebTokenType;

public class MyCloudWebToken extends AbstractWebToken {

    private String pearsonUid;
    private long validUntilTs;

    public MyCloudWebToken(String token) {
        super(token);
    }

    @Override
    public WebTokenType getWebTokenType() {
        return WebTokenType.MY_CLOUD;
    }

    public String getPearsonUid() {
        return pearsonUid;
    }

    public MyCloudWebToken setPearsonUid(String pearsonUid) {
        this.pearsonUid = pearsonUid;
        return this;
    }

    @Override
    public long getValidUntilTs() {
        return validUntilTs;
    }

    public MyCloudWebToken setValidUntilTs(long validUntilTs) {
        this.validUntilTs = validUntilTs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MyCloudWebToken that = (MyCloudWebToken) o;
        return validUntilTs == that.validUntilTs &&
                Objects.equals(pearsonUid, that.pearsonUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pearsonUid, validUntilTs);
    }

    @Override
    public String toString() {
        return "MyCloudWebToken{" +
                "pearsonUid='" + pearsonUid + '\'' +
                ", validUntilTs=" + validUntilTs +
                '}';
    }
}
