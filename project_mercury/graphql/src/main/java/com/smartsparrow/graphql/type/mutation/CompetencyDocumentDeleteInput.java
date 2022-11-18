package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "competencyDocumentDelete", description = "Arguments for deleting a document")
public class CompetencyDocumentDeleteInput {

    private UUID workspaceId;
    private UUID documentId;

    @GraphQLInputField(name = "workspaceId", description = "The workspace where the document will be deleted")
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    @GraphQLInputField(name = "documentId", description = "The id of the document to delete")
    public UUID getDocumentId() {
        return documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentDeleteInput that = (CompetencyDocumentDeleteInput) o;
        return Objects.equals(workspaceId, that.workspaceId) && Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, documentId);
    }

    @Override
    public String toString() {
        return "CompetencyDocumentDeleteInput{" +
                "workspaceId=" + workspaceId +
                ", documentId=" + documentId +
                '}';
    }
}
