package com.smartsparrow.asset.service;

import java.util.Objects;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetSource;

public class BronteAssetEmptyResponse implements BronteAssetResponse {

    private AssetSource assetSource;
    private AssetMediaType assetMediaType;

    @Override
    public AssetSource getAssetSource() {
        return assetSource;
    }

    public BronteAssetEmptyResponse setAssetSource(final AssetSource assetSource) {
        this.assetSource = assetSource;
        return this;
    }

    @Override
    public AssetMediaType getAssetMediaType() {
        return assetMediaType;
    }

    public BronteAssetEmptyResponse setAssetMediaType(final AssetMediaType assetMediaType) {
        this.assetMediaType = assetMediaType;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BronteAssetEmptyResponse that = (BronteAssetEmptyResponse) o;
        return Objects.equals(assetSource, that.assetSource) &&
                assetMediaType == that.assetMediaType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetSource, assetMediaType);
    }

    @Override
    public String toString() {
        return "BronteAssetEmptyResult{" +
                "assetSource=" + assetSource +
                ", assetMediaType=" + assetMediaType +
                '}';
    }
}
