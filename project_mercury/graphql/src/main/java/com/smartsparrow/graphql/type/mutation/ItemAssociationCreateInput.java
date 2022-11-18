package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.competency.data.AssociationType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "CompetencyItemAssociationCreate", description = "Arguments for creating new association")
public class ItemAssociationCreateInput {

    private UUID documentId;
    private UUID originItemId;
    private UUID destinationItemId;
    private AssociationType associationType;

    @GraphQLInputField(description = "The document a new association should belong to")
    public UUID getDocumentId() {
        return documentId;
    }

    @GraphQLInputField(description = "The origin item for an association")
    public UUID getOriginItemId() {
        return originItemId;
    }

    @GraphQLInputField(description = "The destination item for an association")
    public UUID getDestinationItemId() {
        return destinationItemId;
    }

    @GraphQLInputField(description = "The association type")
    public AssociationType getAssociationType() {
        return associationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemAssociationCreateInput that = (ItemAssociationCreateInput) o;
        return Objects.equals(documentId, that.documentId) &&
                Objects.equals(originItemId, that.originItemId) &&
                Objects.equals(destinationItemId, that.destinationItemId) &&
                associationType == that.associationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, originItemId, destinationItemId, associationType);
    }
}
