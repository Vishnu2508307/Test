package com.smartsparrow.learner.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "The published competency document")
public class LearnerDocumentPayload {

    private UUID documentId;
    private String title;
    private String createdAt;
    private String modifiedAt;
    private UUID createdBy;
    private UUID modifiedBy;

    public static LearnerDocumentPayload from(@Nonnull LearnerDocument document) {
        LearnerDocumentPayload payload = new LearnerDocumentPayload();
        payload.documentId = document.getId();
        payload.title = document.getTitle();
        payload.createdAt = DateFormat.asRFC1123(document.getCreatedAt());
        payload.modifiedAt = document.getModifiedAt() == null ? null : DateFormat.asRFC1123(document.getModifiedAt());
        payload.createdBy = document.getCreatedBy();
        payload.modifiedBy = document.getModifiedBy();
        return payload;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerDocumentPayload payload = (LearnerDocumentPayload) o;
        return Objects.equals(documentId, payload.documentId) &&
                Objects.equals(title, payload.title) &&
                Objects.equals(createdAt, payload.createdAt) &&
                Objects.equals(modifiedAt, payload.modifiedAt) &&
                Objects.equals(createdBy, payload.createdBy) &&
                Objects.equals(modifiedBy, payload.modifiedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, title, createdAt, modifiedAt, createdBy, modifiedBy);
    }

    @Override
    public String toString() {
        return "LearnerDocumentPayload{" +
                "documentId=" + documentId +
                ", title='" + title + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", modifiedAt='" + modifiedAt + '\'' +
                ", createdBy=" + createdBy +
                ", modifiedBy=" + modifiedBy +
                '}';
    }
}
