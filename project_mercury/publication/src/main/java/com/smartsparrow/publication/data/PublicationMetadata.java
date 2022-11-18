package com.smartsparrow.publication.data;

import java.util.Objects;
import java.util.UUID;

public class PublicationMetadata {

    private UUID publicationId;
    private String author;
    private String etextVersion;
    private String bookId;
    private UUID createdBy;
    private UUID createdAt;
    private UUID updatedBy;
    private UUID updatedAt;

    public UUID getPublicationId() {
        return publicationId;
    }

    public PublicationMetadata setPublicationId(UUID publicationId) {
        this.publicationId = publicationId;
        return this;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public PublicationMetadata setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public PublicationMetadata setAuthor(final String author) {
        this.author = author;
        return this;
    }

    public UUID getCreatedAt() {
        return createdAt;
    }

    public PublicationMetadata setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public PublicationMetadata setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public UUID getUpdatedAt() {
        return updatedAt;
    }

    public PublicationMetadata setUpdatedAt(UUID updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public String getEtextVersion() {
        return etextVersion;
    }

    public PublicationMetadata setEtextVersion(String etextVersion) {
        this.etextVersion = etextVersion;
        return this;
    }

    public String getBookId() { return bookId; }

    public PublicationMetadata setBookId(String bookId) {
        this.bookId = bookId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationMetadata that = (PublicationMetadata) o;
        return Objects.equals(publicationId, that.publicationId) &&
                Objects.equals(author, that.author) &&
                Objects.equals(etextVersion, that.etextVersion) &&
                Objects.equals(bookId, that.bookId) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId, author, etextVersion, bookId, createdBy, createdAt, updatedBy, updatedAt);
    }

    @Override
    public String toString() {
        return "PublicationMetadata{" +
                "publicationId=" + publicationId +
                ", author=" + author +
                ", etextVersion=" + etextVersion +
                ", bookId=" + bookId +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedBy=" + updatedBy +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
