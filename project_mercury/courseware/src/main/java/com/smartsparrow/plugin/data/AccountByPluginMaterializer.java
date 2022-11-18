package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class AccountByPluginMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountByPluginMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByPlugin(UUID pluginId) {
        final String QUERY = "SELECT "
                + "  plugin_id"
                + ", account_id"
                + ", permission_level"
                + " FROM workspace.account_by_plugin"
                + " WHERE plugin_id=?";
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchByPluginAccount(UUID pluginId, UUID accountId) {
        final String QUERY = "SELECT "
                + "  plugin_id"
                + ", account_id"
                + ", permission_level"
                + " FROM workspace.account_by_plugin"
                + " WHERE plugin_id=? "
                + " AND account_id=?";
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

}
