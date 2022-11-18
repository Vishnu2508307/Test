package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ProjectByWorkspaceMutator extends SimpleTableMutator<ProjectByWorkspace> {

    @Override
    public String getUpsertQuery(final ProjectByWorkspace mutation) {
        return "INSERT INTO workspace.project_by_workspace (" +
                " workspace_id" +
                ", project_id" +
                ", name" +
                ", created_at" +
                ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ProjectByWorkspace mutation) {
        stmt.bind(
                mutation.getWorkspaceId(),
                mutation.getProjectId(),
                mutation.getName(),
                mutation.getCreatedAt()
        );
    }

    @Override
    public String getDeleteQuery(final ProjectByWorkspace mutation) {
        return "DELETE FROM workspace.project_by_workspace" +
                " WHERE workspace_id = ?" +
                " AND project_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final ProjectByWorkspace mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getProjectId());
    }
}
