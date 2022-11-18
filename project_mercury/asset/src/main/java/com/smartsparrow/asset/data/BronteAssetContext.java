package com.smartsparrow.asset.data;

import java.util.Map;
import java.util.Objects;

public class BronteAssetContext {

    private String assetUrn;
    private AssetSource assetSource;
    private Map<String, String> metadata;
    private AssetMediaType assetMediaType;

    public String getAssetUrn() {
        return assetUrn;
    }

    public BronteAssetContext setAssetUrn(final String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public AssetSource getAssetSource() {
        return assetSource;
    }

    public BronteAssetContext setAssetSource(final AssetSource assetSource) {
        this.assetSource = assetSource;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public BronteAssetContext setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public AssetMediaType getAssetMediaType() {
        return assetMediaType;
    }

    public BronteAssetContext setAssetMediaType(final AssetMediaType assetMediaType) {
        this.assetMediaType = assetMediaType;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BronteAssetContext that = (BronteAssetContext) o;
        return Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(assetSource, that.assetSource) &&
                Objects.equals(metadata, that.metadata) &&
                assetMediaType == that.assetMediaType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetUrn, assetSource, metadata, assetMediaType);
    }

    @Override
    public String toString() {
        return "BronteAssetContext{" +
                "assetUrn='" + assetUrn + '\'' +
                ", assetSource=" + assetSource +
                ", metadata=" + metadata +
                ", assetMediaType=" + assetMediaType +
                '}';
    }
}
