package com.smartsparrow.asset.data;

import java.util.UUID;

import com.google.common.base.Objects;

public class DocumentSource implements AssetSource {

    private UUID assetId;
    private String url;

    @Override
    public UUID getAssetId() {
        return assetId;
    }

    public DocumentSource setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DocumentSource setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DocumentSource that = (DocumentSource) o;
        return Objects.equal(getAssetId(), that.getAssetId()) && Objects.equal(getUrl(), that.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getAssetId(), getUrl());
    }

    @Override
    public String toString() {
        return "DocumentSource{" + "assetId=" + assetId + ", url='" + url + '\'' + '}';
    }
}
