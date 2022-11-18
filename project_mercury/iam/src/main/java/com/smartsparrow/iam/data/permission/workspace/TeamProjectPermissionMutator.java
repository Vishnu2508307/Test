package com.smartsparrow.iam.data.permission.workspace;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class TeamProjectPermissionMutator extends SimpleTableMutator<TeamProjectPermission> {

    @Override
    public String getUpsertQuery(final TeamProjectPermission mutation) {
        return "INSERT INTO iam_global.project_permission_by_team (" +
                "team_id" +
                ", project_id" +
                ", permission_level" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final TeamProjectPermission mutation) {
        stmt.bind(
                mutation.getTeamId(),
                mutation.getProjectId(),
                Enums.asString(mutation.getPermissionLevel())
        );
    }

    @Override
    public String getDeleteQuery(final TeamProjectPermission mutation) {
        return "DELETE FROM iam_global.project_permission_by_team" +
                " WHERE team_id = ?" +
                " AND project_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final TeamProjectPermission mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getProjectId());
    }
}
