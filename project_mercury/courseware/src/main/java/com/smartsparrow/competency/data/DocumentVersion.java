package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

public class DocumentVersion {

    private UUID documentId;
    private UUID versionId;
    private UUID authorId;

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentVersion setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public DocumentVersion setVersionId(UUID versionId) {
        this.versionId = versionId;
        return this;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public DocumentVersion setAuthorId(UUID authorId) {
        this.authorId = authorId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentVersion that = (DocumentVersion) o;
        return Objects.equals(documentId, that.documentId) &&
                Objects.equals(versionId, that.versionId) &&
                Objects.equals(authorId, that.authorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, versionId, authorId);
    }
}
