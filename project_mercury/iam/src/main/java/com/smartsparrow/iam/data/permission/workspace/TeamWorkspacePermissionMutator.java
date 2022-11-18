package com.smartsparrow.iam.data.permission.workspace;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamWorkspacePermissionMutator extends SimpleTableMutator<TeamWorkspacePermission> {

    @Override
    public String getUpsertQuery(TeamWorkspacePermission mutation) {
        return "INSERT INTO iam_global.workspace_permission_by_team ("
                + "  team_id"
                + ", workspace_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamWorkspacePermission mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getWorkspaceId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(TeamWorkspacePermission mutation) {
        return "DELETE FROM iam_global.workspace_permission_by_team " +
                "WHERE team_id = ? " +
                "AND workspace_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamWorkspacePermission mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getWorkspaceId());
    }
}
