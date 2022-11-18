package com.smartsparrow.workspace.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PublishMetadataConfig {
    @JsonProperty("masteringLabMetadataUrl")
    private String masteringLabMetadataUrl;

    public String getMasteringLabMetadataUrl() {
        return masteringLabMetadataUrl;
    }

    public PublishMetadataConfig setMasteringLabMetadataUrl(String masteringLabMetadataUrl) {
        this.masteringLabMetadataUrl = masteringLabMetadataUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublishMetadataConfig that = (PublishMetadataConfig) o;
        return Objects.equals(masteringLabMetadataUrl, that.masteringLabMetadataUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masteringLabMetadataUrl);
    }

    @Override
    public String toString() {
        return "PublishMetadataConfig{" +
                "masteringLabMetadataUrl='" + masteringLabMetadataUrl + '\'' +
                '}';
    }
}
