package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class VersionByPluginMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    private static final String SELECT = "SELECT plugin_id, " +
            "major, " +
            "minor, " +
            "patch, " +
            "release_date, " +
            "pre_release, " +
            "build, " +
            "unpublished FROM plugin.version_by_plugin ";

    @Inject
    public VersionByPluginMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchAllPluginVersions(UUID pluginId) {
        final String WHERE = "WHERE plugin_id=?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT + WHERE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchAllPluginMinorReleases(UUID pluginId, int major) {
        final String WHERE = "WHERE plugin_id=? AND major=?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT + WHERE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, major);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchAllPluginPatches(UUID pluginId, int major, int minor) {
        final String WHERE = "WHERE plugin_id=? " +
                "AND major=? " +
                "AND minor=?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT + WHERE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, major, minor);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchAllPluginPreReleases(UUID pluginId, int major, int minor, int patch) {
        final String WHERE = "WHERE plugin_id=? " +
                "AND major=? " +
                "AND minor=? " +
                "AND patch=?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT + WHERE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, major, minor, patch);
        stmt.setIdempotent(true);
        return stmt;
    }

}
