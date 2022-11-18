package com.smartsparrow.sso.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OIDCInfraResponse {

    @JsonProperty("redirectUrl")
    private String redirectUrl;
    @JsonProperty("callbackUrl")
    private String callbackUrl;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public OIDCInfraResponse setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public OIDCInfraResponse setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OIDCInfraResponse that = (OIDCInfraResponse) o;
        return Objects.equals(redirectUrl, that.redirectUrl) && Objects.equals(callbackUrl,
                                                                               that.callbackUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redirectUrl, callbackUrl);
    }

    @Override
    public String toString() {
        return "OIDCInfraResponse{" +
                "redirectUrl='" + redirectUrl + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                '}';
    }
}
