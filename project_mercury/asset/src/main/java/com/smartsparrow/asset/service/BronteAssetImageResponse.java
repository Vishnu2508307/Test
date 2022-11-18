package com.smartsparrow.asset.service;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetSource;

public class BronteAssetImageResponse implements BronteAssetResponse {

    private UUID assetId;
    private AssetSource assetSource;
    private AssetMediaType assetMediaType;

    @Override
    public AssetSource getAssetSource() {
        return assetSource;
    }

    @Override
    public AssetMediaType getAssetMediaType() {
        return assetMediaType;
    }

    public BronteAssetImageResponse setAssetMediaType(final AssetMediaType assetMediaType) {
        this.assetMediaType = assetMediaType;
        return this;
    }

    public BronteAssetImageResponse setAssetSource(final AssetSource assetSource) {
        this.assetSource = assetSource;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public BronteAssetImageResponse setAssetId(final UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BronteAssetImageResponse that = (BronteAssetImageResponse) o;
        return Objects.equals(assetId, that.assetId) &&
                Objects.equals(assetSource, that.assetSource) &&
                assetMediaType == that.assetMediaType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, assetSource, assetMediaType);
    }

    @Override
    public String toString() {
        return "BronteAssetImageResponse{" +
                "assetId=" + assetId +
                ", assetSource=" + assetSource +
                ", assetMediaType=" + assetMediaType +
                '}';
    }
}
