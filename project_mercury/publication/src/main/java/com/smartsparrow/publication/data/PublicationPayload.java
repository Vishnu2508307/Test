package com.smartsparrow.publication.data;

import com.smartsparrow.publication.job.enums.JobStatus;

import java.util.Objects;
import java.util.UUID;

public class PublicationPayload {
    private UUID publicationId;
    private String title;
    private String description;
    private String config;
    private String author;
    private UUID publishedBy;
    private String publisherFamilyName;
    private String publisherGivenName;
    private UUID updatedAt;
    private UUID activityId;
    private String etextVersion;
    private String bookId;
    private UUID jobId;
    private JobStatus publicationJobStatus;
    private String statusMessage;
    private String version;

    public PublicationPayload() {
    }

    public UUID getPublicationId() {
        return publicationId;
    }

    public PublicationPayload setPublicationId(UUID publicationId) {
        this.publicationId = publicationId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PublicationPayload setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PublicationPayload setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public PublicationPayload setConfig(String config) {
        this.config = config;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public PublicationPayload setAuthor(String author) {
        this.author = author;
        return this;
    }

    public UUID getPublishedBy() {
        return publishedBy;
    }

    public PublicationPayload setPublishedBy(UUID publishedBy) {
        this.publishedBy = publishedBy;
        return this;
    }

    public String getPublisherFamilyName() {
        return publisherFamilyName;
    }

    public PublicationPayload setPublisherFamilyName(String publisherFamilyName) {
        this.publisherFamilyName = publisherFamilyName;
        return this;
    }

    public String getPublisherGivenName() {
        return publisherGivenName;
    }

    public PublicationPayload setPublisherGivenName(String publisherGivenName) {
        this.publisherGivenName = publisherGivenName;
        return this;
    }

    public UUID getUpdatedAt() {
        return updatedAt;
    }

    public PublicationPayload setUpdatedAt(UUID updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public PublicationPayload setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public String getEtextVersion() {
        return etextVersion;
    }

    public PublicationPayload setEtextVersion(String etextVersion) {
        this.etextVersion = etextVersion;
        return this;
    }

    public String getBookId() {
        return bookId;
    }

    public PublicationPayload setBookId(String bookId) {
        this.bookId = bookId;
        return this;
    }

    public UUID getJobId() {
        return jobId;
    }

    public PublicationPayload setJobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    public JobStatus getPublicationJobStatus() {
        return publicationJobStatus;
    }

    public PublicationPayload setPublicationJobStatus(JobStatus publicationJobStatus) {
        this.publicationJobStatus = publicationJobStatus;
        return this;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public PublicationPayload setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PublicationPayload setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationPayload that = (PublicationPayload) o;
        return Objects.equals(publicationId, that.publicationId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(config, that.config) &&
                Objects.equals(author, that.author) &&
                Objects.equals(publishedBy, that.publishedBy) &&
                Objects.equals(publisherFamilyName, that.publisherFamilyName) &&
                Objects.equals(publisherGivenName, that.publisherGivenName) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(activityId, that.activityId) &&
                Objects.equals(etextVersion, that.etextVersion) &&
                Objects.equals(bookId, that.bookId) &&
                Objects.equals(jobId, that.jobId) &&
                Objects.equals(publicationJobStatus, that.publicationJobStatus) &&
                Objects.equals(statusMessage, that.statusMessage) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId, title, description, config, author, publishedBy, publisherFamilyName, publisherGivenName, updatedAt, activityId, bookId, etextVersion,
                jobId, publicationJobStatus, statusMessage);
    }

    @Override
    public String toString() {
        return "PublicationWithMetadata{" +
                "publicationId=" + publicationId +
                ", title=" + title +
                ", description=" + description +
                ", config=" + config +
                ", author=" + author +
                ", publishedBy=" + publishedBy +
                ", publisherFamilyName=" + publisherFamilyName +
                ", publisherGivenName= " + publisherGivenName +
                ", updatedAt=" + updatedAt +
                ", activityId=" + activityId +
                ", etextVersion=" + etextVersion +
                ", bookId=" + bookId +
                ", jobId=" + jobId +
                ", publicationJobStatus=" + publicationJobStatus +
                ", statusMessage=" + statusMessage +
                ", version=" + version +
                '}';
    }
}
