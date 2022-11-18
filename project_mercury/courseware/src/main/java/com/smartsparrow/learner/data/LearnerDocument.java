package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

public class LearnerDocument {

    private UUID id;
    private String title;
    private UUID createdAt;
    private UUID createdBy;
    private UUID modifiedAt;
    private UUID modifiedBy;
    private UUID documentVersionId;
    private String origin;

    public UUID getId() {
        return id;
    }

    public LearnerDocument setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public LearnerDocument setTitle(String title) {
        this.title = title;
        return this;
    }

    public UUID getCreatedAt() {
        return createdAt;
    }

    public LearnerDocument setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LearnerDocument setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public UUID getModifiedAt() {
        return modifiedAt;
    }

    public LearnerDocument setModifiedAt(UUID modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    public UUID getModifiedBy() {
        return modifiedBy;
    }

    public LearnerDocument setModifiedBy(UUID modifiedBy) {
        this.modifiedBy = modifiedBy;
        return this;
    }

    public UUID getDocumentVersionId() {
        return documentVersionId;
    }

    public LearnerDocument setDocumentVersionId(UUID documentVersionId) {
        this.documentVersionId = documentVersionId;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public LearnerDocument setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerDocument that = (LearnerDocument) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(modifiedAt, that.modifiedAt) &&
                Objects.equals(modifiedBy, that.modifiedBy) &&
                Objects.equals(documentVersionId, that.documentVersionId) &&
                Objects.equals(origin, that.origin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, createdAt, createdBy, modifiedAt, modifiedBy, documentVersionId, origin);
    }

    @Override
    public String toString() {
        return "LearnerDocument{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                ", modifiedAt=" + modifiedAt +
                ", modifiedBy=" + modifiedBy +
                ", documentVersionId=" + documentVersionId +
                ", origin='" + origin + '\'' +
                '}';
    }
}
