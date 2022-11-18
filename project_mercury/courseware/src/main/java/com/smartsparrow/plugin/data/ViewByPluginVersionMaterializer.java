package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class ViewByPluginVersionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    private static final String SELECT_BY_PLUGIN_VERSION = "SELECT plugin_id, " +
            "version, " +
            "context, " +
            "entry_point_path, " +
            "entry_point_data, " +
            "content_type, " +
            "public_dir, " +
            "editor_mode " +
            "FROM plugin.view_by_plugin_version " +
            "WHERE plugin_id=? " +
            "AND version=?";

    @Inject
    public ViewByPluginVersionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByPluginVersionContext(UUID pluginId, String version, String context) {

        final String FETCH_BY_PLUGIN_VERSION_CONTEXT = SELECT_BY_PLUGIN_VERSION + " " +
                "And context=?";

        BoundStatement stmt = stmtCache.asBoundStatement(FETCH_BY_PLUGIN_VERSION_CONTEXT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, version, context);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchViews(UUID pluginId, String version) {
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_PLUGIN_VERSION);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, version);
        stmt.setIdempotent(true);
        return stmt;
    }

}
