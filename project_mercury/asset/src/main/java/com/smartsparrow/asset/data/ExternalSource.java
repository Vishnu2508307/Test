package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class ExternalSource {

    private UUID assetId;
    private String url;

    public UUID getAssetId() {
        return assetId;
    }

    public ExternalSource setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ExternalSource setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalSource that = (ExternalSource) o;
        return Objects.equals(assetId, that.assetId) &&
                Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, url);
    }

    @Override
    public String toString() {
        return "ExternalSource{" +
                "assetId=" + assetId +
                ", url='" + url + '\'' +
                "} " + super.toString();
    }
}
