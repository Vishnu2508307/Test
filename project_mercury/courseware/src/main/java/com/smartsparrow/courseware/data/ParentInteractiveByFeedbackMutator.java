package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ParentInteractiveByFeedbackMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID feedbackId, UUID interactiveId) {
        // @formatter:off
        String QUERY = "INSERT INTO courseware.parent_interactive_by_feedback ("
                        + "  feedback_id"
                        + ", interactive_id"
                        + ") VALUES ( ?, ? )";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(feedbackId, interactiveId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement delete(UUID feedbackId) {
        // @formatter:off
        String QUERY = "DELETE FROM courseware.parent_interactive_by_feedback"
                        + "  WHERE feedback_id = ?";
        // @formatter:on
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(feedbackId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
