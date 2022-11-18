package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class InteractiveByPluginMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    InteractiveByPluginMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchAllBy(UUID pluginId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " interactive_id"
                + " ,plugin_id"
                + " ,plugin_version_expr"
                + " FROM courseware.interactive_by_plugin"
                + " WHERE plugin_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(pluginId);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("interactive_id");
    }

    public Interactive fromRowToInteractive(Row row) {
        return new Interactive().setId(row.getUUID("interactive_id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersionExpr(row.getString("plugin_version_expr"));
    }
}
