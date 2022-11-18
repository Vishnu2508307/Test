package com.smartsparrow.math.data;

import java.util.Objects;
import java.util.UUID;

public class AssetIdByUrn {

    private String assetUrn;
    private UUID assetId;

    public String getAssetUrn() {
        return assetUrn;
    }

    public AssetIdByUrn setAssetUrn(String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AssetIdByUrn setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetIdByUrn that = (AssetIdByUrn) o;
        return Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetUrn, assetId);
    }

    @Override
    public String toString() {
        return "AssetIdByUrn{" +
                "assetUrn='" + assetUrn + '\'' +
                ", assetId=" + assetId +
                '}';
    }
}
