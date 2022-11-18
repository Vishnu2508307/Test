package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class DeploymentLogMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DeploymentLogMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLast(UUID cohortId, UUID deploymentId, UUID changeId) {
        final String SELECT_LAST = "SELECT" +
                " cohort_id" +
                ", deployment_id" +
                ", change_id" +
                ", id" +
                ", state" +
                ", message" +
                ", element_id" +
                ", element_type" +
                " FROM learner.deployment_log_by_deployment" +
                " WHERE cohort_id = ?" +
                " AND deployment_id = ?" +
                " AND change_id = ?" +
                " LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_LAST);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, deploymentId, changeId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public DeploymentStepLog fromRow(Row row) {
        return new DeploymentStepLog()
                .setDeployment(new Deployment()
                        .setId(row.getUUID("deployment_id"))
                        .setCohortId(row.getUUID("cohort_id"))
                        .setChangeId(row.getUUID("change_id")))
                .setId(row.getUUID("id"))
                .setMessage(row.getString("message"))
                .setElement(new CoursewareElement()
                        .setElementId(row.getUUID("element_id"))
                        .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type"))))
                .setState(Enums.of(DeploymentStepState.class, row.getString("State")));
    }
}