package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ProjectByTeamMutator extends SimpleTableMutator<ProjectTeam> {

    @Override
    public String getUpsertQuery(final ProjectTeam mutation) {
        return "INSERT INTO workspace.project_by_team_workspace (" +
                "team_id" +
                ", workspace_id" +
                ", project_id" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ProjectTeam mutation) {
        stmt.bind(
                mutation.getTeamId(),
                mutation.getWorkspaceId(),
                mutation.getProjectId()
        );
    }

    @Override
    public String getDeleteQuery(final ProjectTeam mutation) {
        return "DELETE FROM workspace.project_by_team_workspace" +
                " WHERE team_id = ?" +
                " AND workspace_id = ?" +
                " AND project_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final ProjectTeam mutation) {
        stmt.bind(
                mutation.getTeamId(),
                mutation.getWorkspaceId(),
                mutation.getProjectId()
        );
    }
}
