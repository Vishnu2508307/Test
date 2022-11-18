package com.smartsparrow.workspace.data;

import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;

import java.util.Objects;
import java.util.UUID;

public class AlfrescoAssetSyncSummary {

    private UUID referenceId;
    private UUID courseId;
    private AlfrescoAssetSyncType syncType;
    private AlfrescoAssetSyncStatus status;
    private UUID completedAt;

    public UUID getReferenceId() {
        return referenceId;
    }

    public AlfrescoAssetSyncSummary setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public AlfrescoAssetSyncSummary setCourseId(UUID courseId) {
        this.courseId = courseId;
        return this;
    }

    public AlfrescoAssetSyncType getSyncType() {
        return syncType;
    }

    public AlfrescoAssetSyncSummary setSyncType(final AlfrescoAssetSyncType syncType) {
        this.syncType = syncType;
        return this;
    }

    public AlfrescoAssetSyncStatus getStatus() {
        return status;
    }

    public AlfrescoAssetSyncSummary setStatus(AlfrescoAssetSyncStatus status) {
        this.status = status;
        return this;
    }

    public UUID getCompletedAt() {
        return completedAt;
    }

    public AlfrescoAssetSyncSummary setCompletedAt(UUID completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoAssetSyncSummary that = (AlfrescoAssetSyncSummary) o;
        return Objects.equals(referenceId, that.referenceId) &&
                Objects.equals(courseId, that.courseId) &&
                syncType == that.syncType &&
                status == that.status &&
                Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceId, courseId, completedAt, syncType, status);
    }

    @Override
    public String toString() {
        return "AlfrescoAssetSyncSummary{" +
                "referenceId=" + referenceId +
                ", courseId=" + courseId +
                ", syncType=" + syncType +
                ", status=" + status +
                ", completedAt=" + completedAt +
                '}';
    }
}
