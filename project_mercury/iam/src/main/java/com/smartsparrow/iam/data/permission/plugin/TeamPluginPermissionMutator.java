package com.smartsparrow.iam.data.permission.plugin;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamPluginPermissionMutator extends SimpleTableMutator<TeamPluginPermission> {

    @Override
    public String getUpsertQuery(TeamPluginPermission mutation) {
        return "INSERT INTO iam_global.plugin_permission_by_team ("
                + "  team_id"
                + ", plugin_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamPluginPermission mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getPluginId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(TeamPluginPermission mutation) {
        return "DELETE FROM iam_global.plugin_permission_by_team " +
                "WHERE team_id = ? " +
                "AND plugin_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamPluginPermission mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getPluginId());
    }
}
