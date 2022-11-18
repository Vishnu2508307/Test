package com.smartsparrow.sso.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class OpenIDConnectConfig {

    @JsonProperty("redirectUrl")
    private String redirectUrl;
    @JsonProperty("callbackUrl")
    private String callbackUrl;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public OpenIDConnectConfig setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public OpenIDConnectConfig setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OpenIDConnectConfig that = (OpenIDConnectConfig) o;
        return Objects.equal(redirectUrl, that.redirectUrl) && Objects.equal(callbackUrl, that.callbackUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(redirectUrl, callbackUrl);
    }

    @Override
    public String toString() {
        return "OpenIDConnectConfig{" + "redirectUrl='" + redirectUrl + '\'' + ", callbackUrl='" + callbackUrl + '\''
                + '}';
    }
}
