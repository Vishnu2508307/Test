package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "Payload for deleting an association mutation")
public class ItemAssociationDeletePayload {

    private UUID associationId;
    private UUID documentId;

    @GraphQLQuery(description = "The deleted association ")
    public UUID getAssociationId() {
        return associationId;
    }

    public ItemAssociationDeletePayload setAssociationId(UUID associationId) {
        this.associationId = associationId;
        return this;
    }

    @GraphQLQuery(description = "The document the association was deleted from")
    public UUID getDocumentId() {
        return documentId;
    }

    public ItemAssociationDeletePayload setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemAssociationDeletePayload that = (ItemAssociationDeletePayload) o;
        return Objects.equals(associationId, that.associationId) &&
                Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(associationId, documentId);
    }
}
