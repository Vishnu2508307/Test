package com.smartsparrow.cohort.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PassportInfraResponse {

    @JsonProperty("baseUrl")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public PassportInfraResponse setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassportInfraResponse that = (PassportInfraResponse) o;
        return Objects.equals(baseUrl, that.baseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl);
    }

    @Override
    public String toString() {
        return "PassportInfraResponse{" +
                "baseUrl='" + baseUrl + '\'' +
                '}';
    }
}
