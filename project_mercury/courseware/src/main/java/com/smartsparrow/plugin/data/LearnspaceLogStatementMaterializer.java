package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class LearnspaceLogStatementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    private static final String BY_PLUGIN_ID = "SELECT " +
            "plugin_id" +
            ", version" +
            ", bucket_id" +
            ", level" +
            ", id" +
            ", message" +
            ", args" +
            ", element_id" +
            ", element_type" +
            ", deployment_id" +
            ", cohort_id" +
            ", plugin_context" +
            "FROM plugin.learnspace_log_statement_by_plugin " +
            "WHERE plugin_id = ?";
    private static final String BY_PLUGIN_ID_VERSION = BY_PLUGIN_ID + " AND version = ?";
    private static final String BY_PLUGIN_ID_BUCKET_ID = BY_PLUGIN_ID + " AND bucket_id = ?";

    @Inject
    public LearnspaceLogStatementMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByPluginId(UUID pluginId) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_PLUGIN_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchByPluginIdAndVersion(UUID pluginId, String version) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_PLUGIN_ID_VERSION);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, version);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchByPluginIdAndBucketId(UUID pluginId, UUID bucketId) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_PLUGIN_ID_BUCKET_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pluginId, bucketId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnspaceLogStatement fromRow(Row row) {
        return new LearnspaceLogStatement()
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("event_type")))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setCohortId(row.getUUID("cohort_id"))
                .setPluginContext(row.getString("plugin_context"))
                .setPluginId(row.getUUID("plugin_id"))
                .setVersion(row.getString("version"))
                .setBucketId(row.getUUID("bucket_id"))
                .setLevel(Enums.of(PluginLogLevel.class, row.getString("level")))
                .setId(row.getUUID("id"))
                .setMessage(row.getString("message"))
                .setArgs(row.getString("args"));
    }
}
