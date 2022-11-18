package com.smartsparrow.sso.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistrarInfraResponse {

    @JsonProperty("baseUrl")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public RegistrarInfraResponse setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrarInfraResponse that = (RegistrarInfraResponse) o;
        return Objects.equals(baseUrl, that.baseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl);
    }

    @Override
    public String toString() {
        return "RegistrarInfraResponse{" +
                "baseUrl='" + baseUrl + '\'' +
                '}';
    }
}
