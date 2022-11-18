package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExternalAsset implements Asset {

    private UUID id;
    private static final AssetProvider assetProvider = AssetProvider.EXTERNAL;
    private AssetMediaType assetMediaType;
    private UUID ownerId;
    private UUID subscriptionId;
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

    public ExternalAsset setId(UUID id) {
        this.id = id;
        return this;
    }

    public ExternalAsset setAssetMediaType(AssetMediaType assetMediaType) {
        this.assetMediaType = assetMediaType;
        return this;
    }

    @Override
    public AssetVisibility getAssetVisibility() {
        return assetVisibility;
    }

    @Override
    public UUID getOwnerId() {
        return ownerId;
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public ExternalAsset setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public ExternalAsset setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public ExternalAsset setAssetVisibility(AssetVisibility assetVisibility) {
        this.assetVisibility = assetVisibility;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalAsset that = (ExternalAsset) o;
        return Objects.equals(id, that.id) &&
                assetMediaType == that.assetMediaType &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                assetVisibility == that.assetVisibility;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assetMediaType, ownerId, subscriptionId, assetVisibility);
    }

    @Override
    public String toString() {
        return "ExternalAsset{" +
                "id=" + id +
                ", assetMediaType=" + assetMediaType +
                ", ownerId=" + ownerId +
                ", subscriptionId=" + subscriptionId +
                ", assetVisibility=" + assetVisibility +
                '}';
    }
}
