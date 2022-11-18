package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "CompetencyDocumentItemDelete", description = "Arguments for deleting a document item")
public class DeleteDocumentItemInput {

    private UUID documentId;
    private UUID id;

    @GraphQLInputField(name = "documentId", description = "The id of the document to delete the item for")
    public UUID getDocumentId() {
        return documentId;
    }

    @GraphQLInputField(name = "id", description = "The id of the document item to delete")
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteDocumentItemInput that = (DeleteDocumentItemInput) o;
        return Objects.equals(documentId, that.documentId) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, id);
    }

    @Override
    public String toString() {
        return "DeleteDocumentItemInput{" +
                "documentId=" + documentId +
                ", id=" + id +
                '}';
    }
}
