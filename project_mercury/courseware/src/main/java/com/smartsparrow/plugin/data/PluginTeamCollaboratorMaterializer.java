package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

public class PluginTeamCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    PluginTeamCollaboratorMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String BY_PLUGIN = "SELECT "
            + "  plugin_id"
            + ", team_id"
            + ", permission_level"
            + " FROM plugin.team_by_plugin"
            + " WHERE plugin_id=?";

    private static final String BY_PLUGIN_TEAM = BY_PLUGIN + " AND team_id=?";

    @SuppressWarnings("Duplicates")
    public Statement fetchByPlugin(UUID pluginId) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_PLUGIN);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchByPluginTeam(UUID pluginId, UUID teamId) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_PLUGIN_TEAM);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public PluginTeamCollaborator fromRow(Row row) {
        return new PluginTeamCollaborator()
                .setTeamId(row.getUUID("team_id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }

}
