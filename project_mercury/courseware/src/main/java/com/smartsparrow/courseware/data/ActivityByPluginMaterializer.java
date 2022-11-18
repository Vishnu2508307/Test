package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;

public class ActivityByPluginMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ActivityByPluginMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchAllBy(UUID pluginId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " activity_id"
                + " ,plugin_id"
                + " ,plugin_version_expr"
                + " FROM courseware.activity_by_plugin"
                + " WHERE plugin_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(pluginId);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("activity_id");
    }

    public Activity fromRowToActivity(Row row) {
        return new Activity().setId(row.getUUID("activity_id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersionExpr(row.getString("plugin_version_expr"));
    }
}
