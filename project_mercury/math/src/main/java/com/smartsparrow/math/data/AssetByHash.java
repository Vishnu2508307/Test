package com.smartsparrow.math.data;

import java.util.Objects;
import java.util.UUID;

public class AssetByHash {

    private String hash;
    private UUID assetId;
    private UUID ownerId;

    public String getHash() {
        return hash;
    }

    public AssetByHash setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AssetByHash setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public AssetByHash setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetByHash that = (AssetByHash) o;
        return Objects.equals(hash, that.hash) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, assetId, ownerId);
    }

    @Override
    public String toString() {
        return "AssetByHash{" +
                "hash='" + hash + '\'' +
                ", assetId=" + assetId +
                ", ownerId=" + ownerId +
                '}';
    }
}
