package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AlfrescoAssetData {

    private UUID assetId;
    private UUID alfrescoId;
    private String name;
    private String version;
    private Long lastModifiedDate;
    private Long lastSyncDate;

    public UUID getAssetId() {
        return assetId;
    }

    public AlfrescoAssetData setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public UUID getAlfrescoId() {
        return alfrescoId;
    }

    public AlfrescoAssetData setAlfrescoId(UUID alfrescoId) {
        this.alfrescoId = alfrescoId;
        return this;
    }

    public String getName() {
        return name;
    }

    public AlfrescoAssetData setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public AlfrescoAssetData setVersion(String version) {
        this.version = version;
        return this;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public AlfrescoAssetData setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public Long getLastSyncDate() {
        return lastSyncDate;
    }

    public AlfrescoAssetData setLastSyncDate(Long lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoAssetData that = (AlfrescoAssetData) o;
        return Objects.equals(assetId, that.assetId) &&
                Objects.equals(alfrescoId, that.alfrescoId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
                Objects.equals(lastSyncDate, that.lastSyncDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, alfrescoId, name, version, lastModifiedDate, lastSyncDate);
    }

    @Override
    public String toString() {
        return "AlfrescoAsset{" +
                "assetId=" + assetId +
                ", id=" + alfrescoId +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                ", lastSyncedDate=" + lastSyncDate +
                '}';
    }
}
