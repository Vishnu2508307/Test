package com.smartsparrow.asset.service;

import java.util.Objects;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetSource;

public class BronteAssetIconResponse implements BronteAssetResponse {

    private String assetUrn;
    private AssetSource assetSource;
    private AssetMediaType assetMediaType;

    @Override
    public AssetSource getAssetSource() {
        return assetSource;
    }

    public BronteAssetIconResponse setAssetSource(final AssetSource assetSource) {
        this.assetSource = assetSource;
        return this;
    }

    @Override
    public AssetMediaType getAssetMediaType() {
        return assetMediaType;
    }

    public BronteAssetIconResponse setAssetMediaType(final AssetMediaType assetMediaType) {
        this.assetMediaType = assetMediaType;
        return this;
    }

    public String getAssetUrn() {
        return assetUrn;
    }

    public BronteAssetIconResponse setAssetUrn(final String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BronteAssetIconResponse that = (BronteAssetIconResponse) o;
        return Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(assetSource, that.assetSource) &&
                assetMediaType == that.assetMediaType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetUrn, assetSource, assetMediaType);
    }

    @Override
    public String toString() {
        return "BronteAssetIconResponse{" +
                "assetUrn='" + assetUrn + '\'' +
                ", assetSource=" + assetSource +
                ", assetMediaType=" + assetMediaType +
                '}';
    }
}
