package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DeploymentByActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DeploymentByActivityMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByActivity(UUID activityId) {
        final String SELECT = "SELECT " +
                "activity_id, " +
                "deployment_id, " +
                "change_id " +
                "FROM learner.deployment_by_activity " +
                "WHERE activity_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(activityId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public DeployedActivity fromRow(Row row) {
        return new DeployedActivity()
                .setActivityId(row.getUUID("activity_id"))
                .setChangeId(row.getUUID("change_id"))
                .setId(row.getUUID("id"));
    }
}
