package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AlfrescoAsset implements Asset {

    private UUID id;
    private static final AssetProvider assetProvider = AssetProvider.ALFRESCO;
    private AssetMediaType assetMediaType;
    private AssetVisibility assetVisibility;
    private UUID ownerId;
    private UUID subscriptionId;

    private UUID alfrescoId;
    private String name;
    private String version;
    private Long lastModifiedDate;
    private Long lastSyncDate;

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

    public AlfrescoAsset setId(UUID id) {
        this.id = id;
        return this;
    }

    public AlfrescoAsset setAssetMediaType(AssetMediaType assetMediaType) {
        this.assetMediaType = assetMediaType;
        return this;
    }

    public UUID getAlfrescoId() {
        return alfrescoId;
    }

    public AlfrescoAsset setAlfrescoId(UUID alfrescoId) {
        this.alfrescoId = alfrescoId;
        return this;
    }

    public String getName() {
        return name;
    }

    public AlfrescoAsset setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public AlfrescoAsset setVersion(String version) {
        this.version = version;
        return this;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public AlfrescoAsset setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public Long getLastSyncDate() {
        return lastSyncDate;
    }

    public AlfrescoAsset setLastSyncDate(Long lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
        return this;
    }

    public AlfrescoAsset setAssetVisibility(AssetVisibility assetVisibility) {
        this.assetVisibility = assetVisibility;
        return this;
    }

    public AlfrescoAsset setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public AlfrescoAsset setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoAsset that = (AlfrescoAsset) o;
        return Objects.equals(id, that.id) &&
                assetMediaType == that.assetMediaType &&
                assetVisibility == that.assetVisibility &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(alfrescoId, that.alfrescoId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
                Objects.equals(lastSyncDate, that.lastSyncDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assetMediaType, assetVisibility, ownerId, subscriptionId, alfrescoId, name, version, lastModifiedDate, lastSyncDate);
    }

    @Override
    public String toString() {
        return "AlfrescoAsset{" +
                "id=" + id +
                ", assetMediaType=" + assetMediaType +
                ", assetVisibility=" + assetVisibility +
                ", ownerId=" + ownerId +
                ", subscriptionId=" + subscriptionId +
                ", alfrescoId=" + alfrescoId +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                ", lastSyncDate=" + lastSyncDate +
                '}';
    }
}
