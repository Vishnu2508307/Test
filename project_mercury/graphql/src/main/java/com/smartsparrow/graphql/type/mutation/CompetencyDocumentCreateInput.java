package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "CompetencyDocumentCreate", description = "Arguments for creating new competency document inside a workspace")
public class CompetencyDocumentCreateInput {

    private UUID workspaceId;
    private String title;

    @GraphQLInputField(name = "workspaceId", description = "The workspace where the new document will be added to")
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    @GraphQLInputField(name = "title", description = "The title for the new document")
    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentCreateInput that = (CompetencyDocumentCreateInput) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, title);
    }
}
