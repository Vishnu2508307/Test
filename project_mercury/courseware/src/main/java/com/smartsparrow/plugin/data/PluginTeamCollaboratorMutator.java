package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class PluginTeamCollaboratorMutator extends SimpleTableMutator<PluginTeamCollaborator> {

    @Override
    public String getUpsertQuery(PluginTeamCollaborator mutation) {
        return "INSERT INTO plugin.team_by_plugin ("
                + "  plugin_id"
                + ", team_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginTeamCollaborator mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getTeamId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(PluginTeamCollaborator mutation) {
        return "DELETE FROM plugin.team_by_plugin " +
                "WHERE plugin_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, PluginTeamCollaborator mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getTeamId());
    }
}
