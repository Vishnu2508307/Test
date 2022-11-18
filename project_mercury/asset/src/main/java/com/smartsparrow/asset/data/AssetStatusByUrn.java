package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetStatusByUrn {
    private UUID id;
    private UUID assetId;
    private String assetUrn;
    private AssetStatus status;

    public UUID getId() {
        return id;
    }

    public AssetStatusByUrn setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AssetStatusByUrn setAssetId(final UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getAssetUrn() {
        return assetUrn;
    }

    public AssetStatusByUrn setAssetUrn(final String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public AssetStatusByUrn setStatus(final AssetStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetStatusByUrn that = (AssetStatusByUrn) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assetId, assetUrn, status);
    }

    @Override
    public String toString() {
        return "AssetStatusByUrn{" +
                "id=" + id +
                ", assetId=" + assetId +
                ", assetUrn='" + assetUrn + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
