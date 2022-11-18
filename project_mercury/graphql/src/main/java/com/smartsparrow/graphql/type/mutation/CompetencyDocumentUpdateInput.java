package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "competencyDocumentUpdate", description = "Arguments for updating a document")
public class CompetencyDocumentUpdateInput {

    private UUID workspaceId;
    private UUID documentId;
    private String title;

    @GraphQLInputField(name = "workspaceId", description = "The workspace where the document will be updated")
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    @GraphQLInputField(name = "documentId", description = "The id of the document to update")
    public UUID getDocumentId() {
        return documentId;
    }

    @GraphQLInputField(name = "title", description = "The title of the document to update")
    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentUpdateInput that = (CompetencyDocumentUpdateInput) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, documentId, title);
    }

    @Override
    public String toString() {
        return "CompetencyDocumentUpdateInput{" +
                "workspaceId=" + workspaceId +
                "documentId=" + documentId +
                ", title='" + title + '\'' +
                '}';
    }
}
