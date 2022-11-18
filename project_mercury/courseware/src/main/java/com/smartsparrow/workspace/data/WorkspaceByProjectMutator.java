package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspaceByProjectMutator extends SimpleTableMutator<WorkspaceProject> {

    @Override
    public String getUpsertQuery(final WorkspaceProject mutation) {
        return "INSERT INTO workspace.workspace_by_project (" +
                " project_id" +
                ", workspace_id" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final WorkspaceProject mutation) {
        stmt.bind(mutation.getProjectId(), mutation.getWorkspaceId());
    }

    @Override
    public String getDeleteQuery(final WorkspaceProject mutation) {
        return "DELETE FROM workspace.workspace_by_project" +
                " WHERE project_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final WorkspaceProject mutation) {
        stmt.bind(mutation.getProjectId());
    }
}
