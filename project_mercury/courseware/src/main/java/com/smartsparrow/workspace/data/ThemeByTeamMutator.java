package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ThemeByTeamMutator extends SimpleTableMutator<ThemeByTeam> {

    @Override
    public String getUpsertQuery(ThemeByTeam mutation) {
        return "INSERT INTO workspace.theme_by_team ("
                + "  team_id"
                + ", theme_id"
                + ") VALUES ( ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ThemeByTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getThemeId());
    }

    @Override
    public String getDeleteQuery(ThemeByTeam mutation) {
        return "DELETE FROM workspace.theme_by_team " +
                "WHERE team_id = ? " +
                "AND theme_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ThemeByTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getThemeId());
    }
}
