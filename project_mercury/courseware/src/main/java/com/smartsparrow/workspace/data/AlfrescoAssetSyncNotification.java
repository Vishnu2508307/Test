package com.smartsparrow.workspace.data;

import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;

import java.util.Objects;
import java.util.UUID;

public class AlfrescoAssetSyncNotification {

    private UUID notificationId;
    private UUID referenceId;
    private UUID courseId;
    private UUID assetId;
    private AlfrescoAssetSyncType syncType;
    private AlfrescoAssetSyncStatus status;
    private UUID completedAt;

    public UUID getNotificationId() {
        return notificationId;
    }

    public AlfrescoAssetSyncNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public AlfrescoAssetSyncNotification setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public AlfrescoAssetSyncNotification setCourseId(UUID courseId) {
        this.courseId = courseId;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AlfrescoAssetSyncNotification setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public AlfrescoAssetSyncType getSyncType() {
        return syncType;
    }

    public AlfrescoAssetSyncNotification setSyncType(final AlfrescoAssetSyncType syncType) {
        this.syncType = syncType;
        return this;
    }

    public AlfrescoAssetSyncStatus getStatus() {
        return status;
    }

    public AlfrescoAssetSyncNotification setStatus(AlfrescoAssetSyncStatus status) {
        this.status = status;
        return this;
    }

    public UUID getCompletedAt() {
        return completedAt;
    }

    public AlfrescoAssetSyncNotification setCompletedAt(UUID completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoAssetSyncNotification that = (AlfrescoAssetSyncNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(referenceId, that.referenceId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(assetId, that.assetId) &&
                syncType == that.syncType &&
                status == that.status &&
                Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, referenceId, courseId, assetId, completedAt, status);
    }

    @Override
    public String toString() {
        return "AlfrescoAssetSyncNotification{" +
                "notificationId=" + notificationId +
                ", referenceId=" + referenceId +
                ", courseId=" + courseId +
                ", assetId=" + assetId +
                ", syncType=" + syncType +
                ", status=" + status +
                ", completedAt=" + completedAt +
                '}';
    }
}
