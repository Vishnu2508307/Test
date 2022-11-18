package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class PluginByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    private static final String QUERY = "SELECT "
            + "  account_id"
            + ", plugin_id"
            + " FROM workspace.plugin_by_account"
            + " WHERE account_id=?";

    @Inject
    PluginByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchByAccount(UUID accountId) {
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
