package com.smartsparrow.rtm.message.recv;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateOIDCProviderMessage extends ReceivedMessage {

    private String issuerUrl;
    private String clientId;
    private String clientSecret;
    private String requestScope;

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRequestScope() {
        return requestScope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateOIDCProviderMessage that = (CreateOIDCProviderMessage) o;
        return Objects.equals(issuerUrl, that.issuerUrl) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientSecret, that.clientSecret) &&
                Objects.equals(requestScope, that.requestScope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuerUrl, clientId, clientSecret, requestScope);
    }

    @Override
    public String toString() {
        return "CreateOIDCProviderMessage{" +
                "issuerUrl='" + issuerUrl + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", requestScope='" + requestScope + '\'' +
                "} " + super.toString();
    }
}
