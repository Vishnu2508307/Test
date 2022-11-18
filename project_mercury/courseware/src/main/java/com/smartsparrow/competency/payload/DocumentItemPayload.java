package com.smartsparrow.competency.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "The competency document item")
public class DocumentItemPayload {

    private UUID id;
    private UUID documentId;
    private String fullStatement;
    private String abbreviatedStatement;
    private String humanCodingScheme;
    private String createdAt;
    private String modifiedAt;

    @SuppressWarnings("Duplicates")
    public static DocumentItemPayload from(@Nonnull DocumentItem item) {
        DocumentItemPayload payload = new DocumentItemPayload();
        payload.id = item.getId();
        payload.documentId = item.getDocumentId();
        payload.fullStatement = item.getFullStatement();
        payload.abbreviatedStatement = item.getAbbreviatedStatement();
        payload.humanCodingScheme = item.getHumanCodingScheme();
        payload.createdAt = item.getCreatedAt() == null ? null : DateFormat.asRFC1123(item.getCreatedAt());
        payload.modifiedAt = item.getModifiedAt() == null ? null : DateFormat.asRFC1123(item.getModifiedAt());
        return payload;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getFullStatement() {
        return fullStatement;
    }

    public String getAbbreviatedStatement() {
        return abbreviatedStatement;
    }

    public String getHumanCodingScheme() {
        return humanCodingScheme;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemPayload that = (DocumentItemPayload) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(fullStatement, that.fullStatement) &&
                Objects.equals(abbreviatedStatement, that.abbreviatedStatement) &&
                Objects.equals(humanCodingScheme, that.humanCodingScheme) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(modifiedAt, that.modifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentId, fullStatement, abbreviatedStatement, humanCodingScheme, createdAt, modifiedAt);
    }

    @Override
    public String toString() {
        return "DocumentItemPayload{" +
                "id=" + id +
                ", documentId=" + documentId +
                ", fullStatement='" + fullStatement + '\'' +
                ", abbreviatedStatement='" + abbreviatedStatement + '\'' +
                ", humanCodingScheme='" + humanCodingScheme + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", modifiedAt='" + modifiedAt + '\'' +
                '}';
    }
}
