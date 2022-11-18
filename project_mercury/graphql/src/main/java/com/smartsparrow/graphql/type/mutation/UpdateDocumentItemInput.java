package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "CompetencyDocumentItemUpdate", description = "Arguments for updating a document item")
public class UpdateDocumentItemInput {

    private UUID documentId;
    private UUID id;
    private String fullStatement;
    private String abbreviatedStatement;
    private String humanCodingScheme;

    @GraphQLInputField(name = "documentId", description = "The id of the document to update the item for")
    public UUID getDocumentId() {
        return documentId;
    }

    @GraphQLInputField(name = "id", description = "The id of the document item to update")
    public UUID getId() {
        return id;
    }

    @GraphQLInputField(name = "fullStatement", description = "The full statement of the document item")
    public String getFullStatement() {
        return fullStatement;
    }

    @GraphQLInputField(name = "abbreviatedStatement", description = "The abbreviated statement")
    public String getAbbreviatedStatement() {
        return abbreviatedStatement;
    }

    @GraphQLInputField(name = "humanCodingScheme", description = "Human coding scheme")
    public String getHumanCodingScheme() {
        return humanCodingScheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateDocumentItemInput that = (UpdateDocumentItemInput) o;
        return Objects.equals(documentId, that.documentId) &&
                Objects.equals(id, that.id) &&
                Objects.equals(fullStatement, that.fullStatement) &&
                Objects.equals(abbreviatedStatement, that.abbreviatedStatement) &&
                Objects.equals(humanCodingScheme, that.humanCodingScheme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, id, fullStatement, abbreviatedStatement, humanCodingScheme);
    }

    @Override
    public String toString() {
        return "UpdateDocumentItemInput{" +
                "documentId=" + documentId +
                ", id=" + id +
                ", fullStatement='" + fullStatement + '\'' +
                ", abbreviatedStatement='" + abbreviatedStatement + '\'' +
                ", humanCodingScheme='" + humanCodingScheme + '\'' +
                '}';
    }
}
