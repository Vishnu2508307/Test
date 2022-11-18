package com.smartsparrow.publication.data;

import java.util.Objects;
import java.util.UUID;

public class PublicationActivityPayload {

    private String title;
    private String author;
    private UUID publishedBy;
    private String publisherFamilyName;
    private String publisherGivenName;
    private UUID updatedAt;
    private UUID activityId;
    private PublicationOutputType outputType;

    public String getTitle() {
        return title;
    }

    public PublicationActivityPayload setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public PublicationActivityPayload setAuthor(String author) {
        this.author = author;
        return this;
    }

    public UUID getPublishedBy() {
        return publishedBy;
    }

    public PublicationActivityPayload setPublishedBy(UUID publishedBy) {
        this.publishedBy = publishedBy;
        return this;
    }

    public String getPublisherFamilyName() {
        return publisherFamilyName;
    }

    public PublicationActivityPayload setPublisherFamilyName(String publisherFamilyName) {
        this.publisherFamilyName = publisherFamilyName;
        return this;
    }

    public String getPublisherGivenName() {
        return publisherGivenName;
    }

    public PublicationActivityPayload setPublisherGivenName(String publisherGivenName) {
        this.publisherGivenName = publisherGivenName;
        return this;
    }

    public UUID getUpdatedAt() {
        return updatedAt;
    }

    public PublicationActivityPayload setUpdatedAt(UUID updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public PublicationActivityPayload setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public PublicationOutputType getOutputType() {
        return outputType;
    }

    public PublicationActivityPayload setOutputType(PublicationOutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationActivityPayload that = (PublicationActivityPayload) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(author, that.author) &&
                Objects.equals(publishedBy, that.publishedBy) &&
                Objects.equals(publisherFamilyName, that.publisherFamilyName) &&
                Objects.equals(publisherGivenName, that.publisherGivenName) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(activityId, that.activityId) &&
                outputType == that.outputType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, publishedBy, publisherFamilyName, publisherGivenName, updatedAt, activityId, outputType);
    }

    @Override
    public String toString() {
        return "PublicationActivityPayload{" +
                "title=" + title +
                ", author=" + author +
                ", publishedBy=" + publishedBy +
                ", publisherFamilyName=" + publisherFamilyName +
                ", publisherGivenName=" + publisherGivenName +
                ", updatedAt=" + updatedAt +
                ", activityId=" + activityId +
                ", outputType=" + outputType +
                '}';
    }
}
