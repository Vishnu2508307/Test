package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

public class DocumentItemReference {

    private UUID documentItemId;
    private UUID documentId;

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    public DocumentItemReference setDocumentItemId(UUID documentItemId) {
        this.documentItemId = documentItemId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentItemReference setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemReference that = (DocumentItemReference) o;
        return Objects.equals(documentItemId, that.documentItemId) &&
                Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItemId, documentId);
    }

    @Override
    public String toString() {
        return "DocumentItemReference{" +
                "documentItemId=" + documentItemId +
                ", documentId=" + documentId +
                '}';
    }
}
