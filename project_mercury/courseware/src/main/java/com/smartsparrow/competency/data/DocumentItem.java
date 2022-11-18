package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;

/**
 * This is information about a specific competency (learning objective) or a grouping of competencies.
 * <br/>
 * NOTE: Not all fields from database table are added here for now as we do not need them at the moment.
 * We will add them to this class later when we start to import third-party CASE documents to the mercury.
 */
public class DocumentItem {

    private UUID id;
    private UUID documentId;
    private String fullStatement;
    private String abbreviatedStatement;
    private String humanCodingScheme;
    private UUID createdById;
    private UUID createdAt;
    private UUID modifiedById;
    private UUID modifiedAt;

    public UUID getId() {
        return id;
    }

    public DocumentItem setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentItem setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getFullStatement() {
        return fullStatement;
    }

    public DocumentItem setFullStatement(String fullStatement) {
        this.fullStatement = fullStatement;
        return this;
    }

    public String getAbbreviatedStatement() {
        return abbreviatedStatement;
    }

    public DocumentItem setAbbreviatedStatement(String abbreviatedStatement) {
        this.abbreviatedStatement = abbreviatedStatement;
        return this;
    }

    public String getHumanCodingScheme() {
        return humanCodingScheme;
    }

    public DocumentItem setHumanCodingScheme(String humanCodingScheme) {
        this.humanCodingScheme = humanCodingScheme;
        return this;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public DocumentItem setCreatedById(UUID createdById) {
        this.createdById = createdById;
        return this;
    }

    @GraphQLIgnore
    public UUID getCreatedAt() {
        return createdAt;
    }

    public DocumentItem setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @GraphQLQuery(name = "createdAt")
    @JsonProperty(value = "createdAt")
    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return null;
        }
        return DateFormat.asRFC1123(createdAt);
    }

    public UUID getModifiedById() {
        return modifiedById;
    }

    public DocumentItem setModifiedById(UUID modifiedById) {
        this.modifiedById = modifiedById;
        return this;
    }

    @GraphQLIgnore
    public UUID getModifiedAt() {
        return modifiedAt;
    }

    public DocumentItem setModifiedAt(UUID modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    @GraphQLQuery(name = "modifiedAt")
    @JsonProperty(value = "modifiedAt")
    public String getFormattedModifiedAt() {
        if(modifiedAt == null) {
            return null;
        }
        return DateFormat.asRFC1123(modifiedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItem caseItem = (DocumentItem) o;
        return Objects.equals(id, caseItem.id) &&
                Objects.equals(documentId, caseItem.documentId) &&
                Objects.equals(fullStatement, caseItem.fullStatement) &&
                Objects.equals(abbreviatedStatement, caseItem.abbreviatedStatement) &&
                Objects.equals(humanCodingScheme, caseItem.humanCodingScheme) &&
                Objects.equals(createdById, caseItem.createdById) &&
                Objects.equals(createdAt, caseItem.createdAt) &&
                Objects.equals(modifiedById, caseItem.modifiedById) &&
                Objects.equals(modifiedAt, caseItem.modifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentId, fullStatement, abbreviatedStatement, humanCodingScheme, createdById, createdAt, modifiedById, modifiedAt);
    }
}
