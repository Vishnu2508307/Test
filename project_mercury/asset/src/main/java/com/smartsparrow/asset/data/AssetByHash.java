package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetByHash {

    private String hash;
    private UUID assetId;
    private AssetProvider provider;
    private UUID subscriptionId;
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

    public AssetProvider getProvider() {
        return provider;
    }

    public AssetByHash setProvider(AssetProvider provider) {
        this.provider = provider;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public AssetByHash setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
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
                provider == that.provider &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, assetId, provider, subscriptionId, ownerId);
    }

    @Override
    public String toString() {
        return "AssetByHash{" +
                "hash='" + hash + '\'' +
                ", assetId=" + assetId +
                ", provider=" + provider +
                ", subscriptionId=" + subscriptionId +
                ", ownerId=" + ownerId +
                '}';
    }
}
