package com.smartsparrow.publication.data;

import java.util.Objects;
import java.util.UUID;

public class PublicationMetadataByPublishedActivity extends PublicationMetadata {

    private UUID activityId;
    private String version;

    public UUID getActivityId() {
        return activityId;
    }

    public PublicationMetadataByPublishedActivity setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PublicationMetadataByPublishedActivity setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PublicationMetadataByPublishedActivity that = (PublicationMetadataByPublishedActivity) o;
        return Objects.equals(activityId, that.activityId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), activityId, version);
    }

    @Override
    public String toString() {
        return "PublicationMetadataByPublishedActivity{" +
                "activityId=" + activityId +
                ", version=" + version +
                ", publicationId=" + super.getPublicationId() +
                ", author=" + super.getAuthor() +
                ", etextVersion=" + super.getEtextVersion() +
                ", bookId=" + super.getBookId() +
                ", createdAt=" + super.getCreatedAt() +
                ", createdBy=" + super.getCreatedBy() +
                ", updatedAt=" + super.getUpdatedAt() +
                ", updatedBy=" + super.getUpdatedBy() +
                '}';
    }
}
