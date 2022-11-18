package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

public class LearnerDocumentItem {

    private UUID id;
    private UUID documentId;
    private String fullStatement;
    private String abbreviatedStatement;
    private String humanCodingScheme;
    private UUID createdBy;
    private UUID createdAt;
    private UUID modifiedBy;
    private UUID modifiedAt;

    public UUID getId() {
        return id;
    }

    public LearnerDocumentItem setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public LearnerDocumentItem setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getFullStatement() {
        return fullStatement;
    }

    public LearnerDocumentItem setFullStatement(String fullStatement) {
        this.fullStatement = fullStatement;
        return this;
    }

    public String getAbbreviatedStatement() {
        return abbreviatedStatement;
    }

    public LearnerDocumentItem setAbbreviatedStatement(String abbreviatedStatement) {
        this.abbreviatedStatement = abbreviatedStatement;
        return this;
    }

    public String getHumanCodingScheme() {
        return humanCodingScheme;
    }

    public LearnerDocumentItem setHumanCodingScheme(String humanCodingScheme) {
        this.humanCodingScheme = humanCodingScheme;
        return this;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LearnerDocumentItem setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public UUID getCreatedAt() {
        return createdAt;
    }

    public LearnerDocumentItem setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UUID getModifiedBy() {
        return modifiedBy;
    }

    public LearnerDocumentItem setModifiedBy(UUID modifiedBy) {
        this.modifiedBy = modifiedBy;
        return this;
    }

    public UUID getModifiedAt() {
        return modifiedAt;
    }

    public LearnerDocumentItem setModifiedAt(UUID modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerDocumentItem that = (LearnerDocumentItem) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(fullStatement, that.fullStatement) &&
                Objects.equals(abbreviatedStatement, that.abbreviatedStatement) &&
                Objects.equals(humanCodingScheme, that.humanCodingScheme) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(modifiedBy, that.modifiedBy) &&
                Objects.equals(modifiedAt, that.modifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentId, fullStatement, abbreviatedStatement, humanCodingScheme, createdBy,
                createdAt, modifiedBy, modifiedAt);
    }

    @Override
    public String toString() {
        return "LearnerDocumentItem{" +
                "id=" + id +
                ", documentId=" + documentId +
                ", fullStatement='" + fullStatement + '\'' +
                ", abbreviatedStatement='" + abbreviatedStatement + '\'' +
                ", humanCodingScheme='" + humanCodingScheme + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", modifiedBy=" + modifiedBy +
                ", modifiedAt=" + modifiedAt +
                '}';
    }
}
