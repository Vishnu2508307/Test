package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class TeamByThemeMutator extends SimpleTableMutator<TeamByTheme> {

    @Override
    public String getUpsertQuery(TeamByTheme mutation) {
        return "INSERT INTO workspace.team_by_theme ("
                + "  theme_id"
                + ", team_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamByTheme mutation) {
        stmt.bind(mutation.getThemeId(), mutation.getTeamId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(TeamByTheme mutation) {
        return "DELETE FROM workspace.team_by_theme " +
                "WHERE theme_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamByTheme mutation) {
        stmt.bind(mutation.getThemeId(), mutation.getTeamId());
    }
}
