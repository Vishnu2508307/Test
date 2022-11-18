package com.smartsparrow.publication.data;

import java.util.Objects;
import java.util.UUID;

public class PublishedActivity {

    private UUID publicationId;
    private UUID activityId;
    private String version;
    private String title;
    private String description;
    private PublicationOutputType outputType;
    private ActivityPublicationStatus status;

    public UUID getPublicationId() {
        return publicationId;
    }

    public PublishedActivity setPublicationId(UUID publicationId) {
        this.publicationId = publicationId;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public PublishedActivity setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PublishedActivity setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PublishedActivity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PublishedActivity setDescription(String description) {
        this.description = description;
        return this;
    }

    public PublicationOutputType getOutputType() {
        return outputType;
    }

    public PublishedActivity setOutputType(PublicationOutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    public ActivityPublicationStatus getStatus() {
        return status;
    }

    public PublishedActivity setStatus(ActivityPublicationStatus publicationSummaryStatus) {
        this.status = publicationSummaryStatus;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublishedActivity that = (PublishedActivity) o;
        return Objects.equals(publicationId, that.publicationId) &&
                Objects.equals(activityId, that.activityId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                status == that.status &&
                outputType == that.outputType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId, activityId, version, title, description, outputType);
    }

    @Override
    public String toString() {
        return "PublicationActivity{" +
                "publicationId=" + publicationId +
                ", activityId=" + activityId +
                ", version=" + version +
                ", title=" + title +
                ", description=" + description +
                ", outputType=" + outputType +
                ", status=" + status +
                '}';
    }
}
