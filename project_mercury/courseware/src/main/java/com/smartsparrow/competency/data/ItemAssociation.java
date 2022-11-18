package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;

/**
 * This is the data about the relationship between two document items.
 * <br/>
 * NOTE: Not all fields from database table are added here for now as we do not need them at the moment.
 * We will add them to this class later when we start to import third-party CASE documents to the mercury.
 */
public class ItemAssociation {

    private UUID id;
    private UUID documentId;
    private UUID originItemId;
    private UUID destinationItemId;
    private AssociationType associationType;
    private UUID createdAt;
    private UUID createdById;

    public UUID getId() {
        return id;
    }

    public ItemAssociation setId(UUID id) {
        this.id = id;
        return this;
    }

    @GraphQLIgnore
    public UUID getDocumentId() {
        return documentId;
    }

    public ItemAssociation setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getOriginItemId() {
        return originItemId;
    }

    public ItemAssociation setOriginItemId(UUID originItemId) {
        this.originItemId = originItemId;
        return this;
    }

    public UUID getDestinationItemId() {
        return destinationItemId;
    }

    public ItemAssociation setDestinationItemId(UUID destinationItemId) {
        this.destinationItemId = destinationItemId;
        return this;
    }

    public AssociationType getAssociationType() {
        return associationType;
    }

    public ItemAssociation setAssociationType(AssociationType associationType) {
        this.associationType = associationType;
        return this;
    }

    @GraphQLIgnore
    @JsonIgnore
    public UUID getCreatedAt() {
        return createdAt;
    }

    public ItemAssociation setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @GraphQLQuery(name = "createdAt")
    @JsonProperty(value = "createdAt")
    public String getFormattedCreatedAt() {
        return DateFormat.asRFC1123(getCreatedAt());
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public ItemAssociation setCreatedById(UUID createdById) {
        this.createdById = createdById;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemAssociation that = (ItemAssociation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(originItemId, that.originItemId) &&
                Objects.equals(destinationItemId, that.destinationItemId) &&
                associationType == that.associationType &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdById, that.createdById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentId, originItemId, destinationItemId, associationType, createdAt, createdById);
    }
}
