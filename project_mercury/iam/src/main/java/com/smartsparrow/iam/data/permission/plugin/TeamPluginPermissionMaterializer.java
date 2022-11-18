package com.smartsparrow.iam.data.permission.plugin;

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

public class TeamPluginPermissionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    TeamPluginPermissionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchPermissionLevel(UUID teamId, UUID pluginId) {
        final String QUERY = "SELECT "
                + "permission_level"
                + " FROM iam_global.plugin_permission_by_team"
                + " WHERE team_id=?"
                + " AND plugin_id=?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId, pluginId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public PermissionLevel fromRow(Row row) {
        return Enums.of(PermissionLevel.class, row.getString("permission_level"));
    }
}
