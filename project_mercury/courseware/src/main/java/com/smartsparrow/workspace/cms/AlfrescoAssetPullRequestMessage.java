package com.smartsparrow.workspace.cms;

import java.util.Objects;
import java.util.UUID;

public class AlfrescoAssetPullRequestMessage {

    private UUID referenceId;
    private UUID ownerId;
    private UUID assetId;
    private String version;
    private UUID alfrescoNodeId;
    private Long lastModified;
    private boolean forceSync;

    /**
     * Forces to fetch the asset content even when there are no updates
     *
     * @return a boolean describing whether the sync should be forced
     */
    public boolean isForceSync() {
        return forceSync;
    }

    public AlfrescoAssetPullRequestMessage setForceSync(boolean forceSync) {
        this.forceSync = forceSync;
        return this;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public AlfrescoAssetPullRequestMessage setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public AlfrescoAssetPullRequestMessage setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AlfrescoAssetPullRequestMessage setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public AlfrescoAssetPullRequestMessage setVersion(String version) {
        this.version = version;
        return this;
    }

    public UUID getAlfrescoNodeId() {
        return alfrescoNodeId;
    }

    public AlfrescoAssetPullRequestMessage setAlfrescoNodeId(UUID alfrescoNodeId) {
        this.alfrescoNodeId = alfrescoNodeId;
        return this;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public AlfrescoAssetPullRequestMessage setLastModified(Long lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoAssetPullRequestMessage that = (AlfrescoAssetPullRequestMessage) o;
        return Objects.equals(referenceId, that.referenceId) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(alfrescoNodeId, that.alfrescoNodeId) &&
                Objects.equals(lastModified, that.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceId, ownerId, assetId, version, alfrescoNodeId, lastModified);
    }

    @Override
    public String toString() {
        return "AlfrescoAssetPullRequestMessage{" +
                "referenceId=" + referenceId +
                ", ownerId=" + ownerId +
                ", assetId=" + assetId +
                ", version='" + version + '\'' +
                ", alfrescoNodeId=" + alfrescoNodeId +
                ", lastModified=" + lastModified +
                ", forceSync=" + forceSync +
                '}';
    }
}
