package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetMetadata {

    private UUID assetId;
    private String key;
    private String value;

    public UUID getAssetId() {
        return assetId;
    }

    public AssetMetadata setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getKey() {
        return key;
    }

    public AssetMetadata setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AssetMetadata setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetMetadata metadata = (AssetMetadata) o;
        return Objects.equals(assetId, metadata.assetId) &&
                Objects.equals(key, metadata.key) &&
                Objects.equals(value, metadata.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, key, value);
    }

    @Override
    public String toString() {
        return "AssetMetadata{" +
                "assetId=" + assetId +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
