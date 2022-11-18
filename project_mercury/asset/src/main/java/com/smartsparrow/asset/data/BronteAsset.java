package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BronteAsset implements Asset {

    private UUID id;
    private static final AssetProvider assetProvider = AssetProvider.AERO;
    private AssetMediaType assetMediaType;

    private UUID ownerId;
    private UUID subscriptionId;
    private String hash;
    private AssetVisibility assetVisibility;

    @Override
    public UUID getId() {
        return id;
    }

    @JsonIgnore
    @Override
    public AssetProvider getAssetProvider() {
        return assetProvider;
    }

    @Override
    public AssetMediaType getAssetMediaType() {
        return assetMediaType;
    }

    public BronteAsset setId(UUID id) {
        this.id = id;
        return this;
    }

    public BronteAsset setAssetMediaType(AssetMediaType assetMediaType) {
        this.assetMediaType = assetMediaType;
        return this;
    }

    @Override
    public UUID getOwnerId() {
        return ownerId;
    }

    public BronteAsset setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public BronteAsset setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public BronteAsset setHash(String hash) {
        this.hash = hash;
        return this;
    }

    @Override
    public AssetVisibility getAssetVisibility() {
        return assetVisibility;
    }

    public BronteAsset setAssetVisibility(AssetVisibility assetVisibility) {
        this.assetVisibility = assetVisibility;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BronteAsset that = (BronteAsset) o;
        return Objects.equals(id, that.id) &&
                assetMediaType == that.assetMediaType &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(hash, that.hash) &&
                assetVisibility == that.assetVisibility;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assetMediaType, ownerId, subscriptionId, hash, assetVisibility);
    }

    @Override
    public String toString() {
        return "BronteAsset{" +
                "id=" + id +
                ", assetMediaType=" + assetMediaType +
                ", ownerId=" + ownerId +
                ", subscriptionId=" + subscriptionId +
                ", hash='" + hash + '\'' +
                ", assetVisibility=" + assetVisibility +
                '}';
    }
}
