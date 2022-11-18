package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
public class DocumentItemInput {

    @GraphQLInputField(name = "documentItemId", description = "id of the document item")
    private UUID documentItemId;

    @GraphQLInputField(name = "documentId", description = "id of the document")
    private UUID documentId;

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemInput that = (DocumentItemInput) o;
        return Objects.equals(documentItemId, that.documentItemId) &&
                Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItemId, documentId);
    }

    @Override
    public String toString() {
        return "DocumentItemInput{" +
                "documentItemId=" + documentItemId +
                ", documentId=" + documentId +
                '}';
    }
}
