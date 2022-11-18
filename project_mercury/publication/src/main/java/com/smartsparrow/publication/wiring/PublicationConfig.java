package com.smartsparrow.publication.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class PublicationConfig {
    @JsonProperty("baseUrl")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public PublicationConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PublicationConfig that = (PublicationConfig) o;
        return Objects.equal(baseUrl, that.baseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(baseUrl);
    }

    @Override
    public String toString() {
        return "PublicationConfig{" + "baseUrl='" + baseUrl + '\'' + '}';
    }
}
