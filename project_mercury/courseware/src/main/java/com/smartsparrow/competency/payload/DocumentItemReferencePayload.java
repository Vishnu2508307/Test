package com.smartsparrow.competency.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.competency.data.DocumentItemTag;

import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "The competency document item reference payload")
public class DocumentItemReferencePayload {

    private UUID documentId;
    private UUID documentItemId;

    public static DocumentItemReferencePayload from(@Nonnull DocumentItemTag documentItemTag) {
        DocumentItemReferencePayload payload = new DocumentItemReferencePayload();
        payload.documentId = documentItemTag.getDocumentId();
        payload.documentItemId = documentItemTag.getDocumentItemId();
        return payload;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemReferencePayload that = (DocumentItemReferencePayload) o;
        return Objects.equals(documentId, that.documentId) &&
                Objects.equals(documentItemId, that.documentItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, documentItemId);
    }

    @Override
    public String toString() {
        return "DocumentItemReferencePayload{" +
                "documentId=" + documentId +
                ", documentItemId=" + documentItemId +
                '}';
    }
}
