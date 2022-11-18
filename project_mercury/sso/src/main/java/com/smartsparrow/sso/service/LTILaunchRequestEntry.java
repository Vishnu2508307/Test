package com.smartsparrow.sso.service;

import java.util.Objects;
import java.util.UUID;

public class LTILaunchRequestEntry {

    public enum Part {
        HEADER,
        PARAM
    }

    private UUID launchRequestId;
    private Part part;
    private String name;
    private String value;
    private String requestUrl;
    private UUID ltiv11CredentialId;
    private Integer ttl;

    public UUID getLaunchRequestId() {
        return launchRequestId;
    }

    public LTILaunchRequestEntry setLaunchRequestId(final UUID launchRequestId) {
        this.launchRequestId = launchRequestId;
        return this;
    }

    public Part getPart() {
        return part;
    }

    public LTILaunchRequestEntry setPart(final Part part) {
        this.part = part;
        return this;
    }

    public String getName() {
        return name;
    }

    public LTILaunchRequestEntry setName(final String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public LTILaunchRequestEntry setValue(final String value) {
        this.value = value;
        return this;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public LTILaunchRequestEntry setRequestUrl(final String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    /**
     * LTI consumer credentials are now saved against a cohortId. This call is deprecated and the column will be dropped
     * soon from the db
     */
    @Deprecated
    public UUID getLtiv11CredentialId() {
        return ltiv11CredentialId;
    }

    /**
     * LTI consumer credentials are now saved against a cohortId, this call is deprecated and the column will be dropped
     * soon from the db
     */
    @Deprecated
    public LTILaunchRequestEntry setLtiv11CredentialId(final UUID ltiv11CredentialId) {
        this.ltiv11CredentialId = ltiv11CredentialId;
        return this;
    }

    public Integer getTtl() {
        return ttl;
    }

    public LTILaunchRequestEntry setTtl(Integer ttl) {
        this.ttl = ttl;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTILaunchRequestEntry that = (LTILaunchRequestEntry) o;
        return Objects.equals(launchRequestId, that.launchRequestId) &&
                part == that.part &&
                Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(requestUrl, that.requestUrl) &&
                Objects.equals(ltiv11CredentialId, that.ltiv11CredentialId) &&
                Objects.equals(ttl, that.ttl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(launchRequestId, part, name, value, requestUrl, ltiv11CredentialId, ttl);
    }

    @Override
    public String toString() {
        return "LTILaunchRequestEntry{" +
                "launchRequestId=" + launchRequestId +
                ", part=" + part +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", requestUrl='" + requestUrl + '\'' +
                ", ltiv11CredentialId=" + ltiv11CredentialId +
                ", ttl=" + ttl +
                '}';
    }
}
