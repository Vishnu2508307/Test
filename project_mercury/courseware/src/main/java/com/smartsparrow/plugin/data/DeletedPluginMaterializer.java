package com.smartsparrow.plugin.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DeletedPluginMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DeletedPluginMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAll() {
        String SELECT = "SELECT id, " +
                "plugin_id, " +
                "account_id " +
                "FROM plugin.deleted";
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        return stmt;
    }

    public DeletedPlugin fromRow(Row row) {
        return new DeletedPlugin()
                .setId(row.getUUID("id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setAccountId(row.getUUID("account_id"));
    }
}
