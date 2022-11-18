package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DeploymentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DeploymentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String SELECT = "SELECT " +
            "id, " +
            "activity_id, " +
            "change_id, " +
            "cohort_id " +
            "FROM learner.deployment " +
            "WHERE id = ? ";

    public Statement findLatest(UUID id) {
        final String SELECT_LATEST = SELECT + "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_LATEST);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findLatestByActivity(UUID id, UUID activityId) {
        final String SELECT_LATEST_BY_ACTIVITY = SELECT +
                "AND activity_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_LATEST_BY_ACTIVITY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id, activityId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findLatestChangeIds(UUID id, int limit) {
        final String SELECT = "SELECT change_id FROM learner.deployment WHERE id = ? LIMIT " + limit;

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findActivityByDeployment(UUID deploymentId) {
        final String SELECT_LATEST_BY_ACTIVITY = SELECT +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_LATEST_BY_ACTIVITY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public DeployedActivity fromRow(Row row) {
        return new DeployedActivity()
                .setActivityId(row.getUUID("activity_id"))
                .setChangeId(row.getUUID("change_id"))
                .setId(row.getUUID("id"))
                .setCohortId(row.getUUID("cohort_id"));
    }
}
