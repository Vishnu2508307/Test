package com.smartsparrow.learner.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "The published competency document item assocaition")
public class LearnerDocumentItemAssociationPayload {

    private UUID associationId;
    private UUID documentId;
    private UUID originItemId;
    private UUID destinationItemId;
    private AssociationType associationType;
    private String createdAt;
    private UUID createdBy;

    public static LearnerDocumentItemAssociationPayload from(@Nonnull ItemAssociation itemAssociation) {
        LearnerDocumentItemAssociationPayload payload = new LearnerDocumentItemAssociationPayload();
        payload.associationId = itemAssociation.getId();
        payload.documentId = itemAssociation.getDocumentId();
        payload.originItemId = itemAssociation.getOriginItemId();
        payload.destinationItemId = itemAssociation.getDestinationItemId();
        payload.associationType = itemAssociation.getAssociationType();
        payload.createdAt = (itemAssociation.getCreatedAt() != null ? DateFormat.asRFC1123(itemAssociation.getCreatedAt()) : null);
        payload.createdBy = itemAssociation.getCreatedById();
        return payload;
    }

    public UUID getAssociationId() {
        return associationId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getOriginItemId() {
        return originItemId;
    }

    public UUID getDestinationItemId() {
        return destinationItemId;
    }

    public AssociationType getAssociationType() {
        return associationType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerDocumentItemAssociationPayload that = (LearnerDocumentItemAssociationPayload) o;
        return Objects.equals(associationId, that.associationId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(originItemId, that.originItemId) &&
                Objects.equals(destinationItemId, that.destinationItemId) &&
                associationType == that.associationType &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdBy, that.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(associationId, documentId, originItemId, destinationItemId, associationType, createdAt, createdBy);
    }

    @Override
    public String toString() {
        return "LearnerDocumentItemAssociationPayload{" +
                "associationId=" + associationId +
                ", documentId=" + documentId +
                ", originItemId=" + originItemId +
                ", destinationItemId=" + destinationItemId +
                ", associationType=" + associationType +
                ", createdAt='" + createdAt + '\'' +
                ", createdBy=" + createdBy +
                '}';
    }
}
