package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspaceTeamCollaboratorMutator extends SimpleTableMutator<WorkspaceTeamCollaborator> {

    @Override
    public String getUpsertQuery(WorkspaceTeamCollaborator mutation) {
        return "INSERT INTO workspace.team_by_workspace ("
                + "  workspace_id"
                + ", team_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, WorkspaceTeamCollaborator mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getTeamId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(WorkspaceTeamCollaborator mutation) {
        return "DELETE FROM workspace.team_by_workspace " +
                "WHERE workspace_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, WorkspaceTeamCollaborator mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getTeamId());
    }
}
