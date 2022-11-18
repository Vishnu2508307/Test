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

public class FeedbackByLearnerInteractiveMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public FeedbackByLearnerInteractiveMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByLatestDeployment(UUID interactiveId, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "feedback_ids " +
                "FROM learner.feedback_by_interactive " +
                "WHERE interactive_id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(interactiveId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public List<UUID> fromRow(Row row) {
        return row.getList("feedback_ids", UUID.class);
    }
}
