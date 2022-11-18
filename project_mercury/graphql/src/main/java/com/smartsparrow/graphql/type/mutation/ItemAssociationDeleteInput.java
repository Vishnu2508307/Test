package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "CompetencyItemAssociationDelete", description = "Arguments for deleting an association")
public class ItemAssociationDeleteInput {

    private UUID associationId;
    private UUID documentId;

    @GraphQLInputField(description = "The association to delete")
    public UUID getAssociationId() {
        return associationId;
    }

    @GraphQLInputField(description = "The document id the association belongs to")
    public UUID getDocumentId() {
        return documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemAssociationDeleteInput that = (ItemAssociationDeleteInput) o;
        return Objects.equals(associationId, that.associationId) &&
                Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(associationId, documentId);
    }
}
