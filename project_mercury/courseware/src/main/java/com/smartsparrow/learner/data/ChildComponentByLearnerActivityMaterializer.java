package com.smartsparrow.learner.data;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ChildComponentByLearnerActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ChildComponentByLearnerActivityMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatestChangeFor(UUID activityId, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "component_ids " +
                "FROM learner.child_component_by_activity " +
                "WHERE activity_id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(activityId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public List<UUID> fromRow(Row row) {
        return row.getList("component_ids", UUID.class);
    }
}
