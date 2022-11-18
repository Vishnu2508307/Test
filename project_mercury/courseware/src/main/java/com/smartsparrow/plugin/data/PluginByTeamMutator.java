package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class PluginByTeamMutator extends SimpleTableMutator<PluginByTeam> {

    @Override
    public String getUpsertQuery(PluginByTeam mutation) {
        return "INSERT INTO plugin.plugin_by_team ("
                + "  team_id"
                + ", plugin_id"
                + ") VALUES ( ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginByTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getPluginId());
    }

    @Override
    public String getDeleteQuery(PluginByTeam mutation) {
        return "DELETE FROM plugin.plugin_by_team " +
                "WHERE team_id = ? " +
                "AND plugin_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, PluginByTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getPluginId());
    }
}
