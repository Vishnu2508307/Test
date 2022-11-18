package com.smartsparrow.iam.data.permission.workspace;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ThemePermissionByTeamMutator extends SimpleTableMutator<ThemePermissionByTeam> {

    @Override
    public String getUpsertQuery(ThemePermissionByTeam mutation) {
        return "INSERT INTO iam_global.theme_permission_by_team ("
                + "  team_id"
                + ", theme_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ThemePermissionByTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getThemeId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(ThemePermissionByTeam mutation) {
        return "DELETE FROM iam_global.theme_permission_by_team " +
                "WHERE team_id = ? " +
                "AND theme_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ThemePermissionByTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getThemeId());
    }
}
