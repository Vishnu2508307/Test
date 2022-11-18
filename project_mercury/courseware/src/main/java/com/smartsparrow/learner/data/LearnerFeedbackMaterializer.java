package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LearnerFeedbackMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerFeedbackMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatestDeployed(UUID id, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "plugin_id, " +
                "plugin_version_expr, " +
                "config " +
                "FROM learner.feedback " +
                "WHERE id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerFeedback fromRow(Row row) {
        return new LearnerFeedback()
                .setId(row.getUUID("id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersionExpr(row.getString("plugin_version_expr"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setConfig(row.getString("config"));
    }
}
