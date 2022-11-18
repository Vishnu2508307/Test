package com.smartsparrow.iam.data.permission.plugin;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class PluginPermissionByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    PluginPermissionByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByAccountPlugin(UUID accountId, UUID pluginId) {
        final String QUERY = "SELECT "
                + "  account_id"
                + ", plugin_id"
                + ", permission_level"
                + " FROM iam_global.plugin_permission_by_account"
                + " WHERE account_id=?"
                + " AND plugin_id=?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, pluginId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchByAccount(UUID accountId) {
        final String QUERY = "SELECT "
                + "  account_id"
                + ", plugin_id"
                + ", permission_level"
                + " FROM iam_global.plugin_permission_by_account"
                + " WHERE account_id=?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;

    }
}
