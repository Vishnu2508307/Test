package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

@Deprecated
public class LearnerActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerActivityMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByDeployment(UUID id, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "plugin_id, " +
                "plugin_version_expr, " +
                "config, " +
                "theme, " +
                "creator_id, " +
                "student_scope_urn, " +
                "evaluation_mode " +
                "FROM learner.activity " +
                "WHERE id = ?" +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerActivity fromRow(Row row) {
        return new LearnerActivity()
                .setId(row.getUUID("id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersionExpr(row.getString("plugin_version_expr"))
                .setConfig(row.getString("config"))
                .setTheme(row.getString("theme"))
                .setCreatorId(row.getUUID("creator_id"))
                .setStudentScopeURN(row.getUUID("student_scope_urn"));
    }
}
