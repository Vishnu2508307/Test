package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspaceByTeamMutator extends SimpleTableMutator<WorkspaceByTeam> {

    @Override
    public String getUpsertQuery(WorkspaceByTeam mutation) {
        return "INSERT INTO workspace.workspace_by_team ("
                + "  team_id"
                + ", workspace_id"
                + ") VALUES ( ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, WorkspaceByTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getWorkspaceId());
    }

    @Override
    public String getDeleteQuery(WorkspaceByTeam mutation) {
        return "DELETE FROM workspace.workspace_by_team " +
                "WHERE team_id = ? " +
                "AND workspace_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, WorkspaceByTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getWorkspaceId());
    }
}
