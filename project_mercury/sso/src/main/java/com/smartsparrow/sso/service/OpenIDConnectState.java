package com.smartsparrow.sso.service;

import java.util.UUID;

import com.google.common.base.Objects;

public class OpenIDConnectState {

    private String state;
    private String redirectUrl;
    private UUID relyingPartyId;
    private String nonce;

    public String getState() {
        return state;
    }

    public OpenIDConnectState setState(String state) {
        this.state = state;
        return this;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public OpenIDConnectState setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    public UUID getRelyingPartyId() {
        return relyingPartyId;
    }

    public OpenIDConnectState setRelyingPartyId(UUID relyingPartyId) {
        this.relyingPartyId = relyingPartyId;
        return this;
    }

    public String getNonce() {
        return nonce;
    }

    public OpenIDConnectState setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OpenIDConnectState that = (OpenIDConnectState) o;
        return Objects.equal(state, that.state) && Objects.equal(redirectUrl, that.redirectUrl) && Objects.equal(
                relyingPartyId, that.relyingPartyId) && Objects.equal(nonce, that.nonce);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(state, redirectUrl, relyingPartyId, nonce);
    }

    @Override
    public String toString() {
        return "OpenIDConnectState{" + "state='" + state + '\'' + ", redirectUrl='" + redirectUrl + '\''
                + ", relyingPartyId='" + relyingPartyId + '\'' + ", nonce='" + nonce + '\'' + '}';
    }
}
