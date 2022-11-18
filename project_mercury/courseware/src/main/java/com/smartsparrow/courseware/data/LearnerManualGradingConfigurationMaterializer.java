package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class LearnerManualGradingConfigurationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerManualGradingConfigurationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAll(UUID deploymentId) {
        final String SELECT = "SELECT " +
                " deployment_id" +
                ", component_id" +
                ", max_score" +
                ", change_id" +
                ", parent_id" +
                ", parent_type" +
                " FROM learner.manual_grading_configuration_by_deployment" +
                " WHERE deployment_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(deploymentId);
        return stmt;
    }

    public Statement find(UUID deploymentId, UUID componentId) {
        final String SELECT = "SELECT " +
                " deployment_id" +
                ", component_id" +
                ", max_score" +
                ", change_id" +
                ", parent_id" +
                ", parent_type" +
                " FROM learner.manual_grading_configuration_by_deployment" +
                " WHERE deployment_id = ?" +
                " AND component_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(deploymentId, componentId);
        return stmt;
    }

    public LearnerManualGradingConfiguration fromRow(Row row) {
        return new LearnerManualGradingConfiguration()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setComponentId(row.getUUID("component_id"))
                .setMaxScore(row.getDouble("max_score"))
                .setChangeId(row.getUUID("change_id"))
                .setParentId(row.getUUID("parent_id"))
                .setParentType(Enums.of(CoursewareElementType.class, row.getString("parent_type")));
    }
}
