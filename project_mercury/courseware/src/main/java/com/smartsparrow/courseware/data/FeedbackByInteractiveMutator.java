package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;

class FeedbackByInteractiveMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID feedbackId, UUID interactiveId) {
        // @formatter:off
        String QUERY = "INSERT INTO courseware.feedback_by_interactive ("
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

    public Statement addFeedback(UUID feedbackId, UUID interactiveId) {
        final String QUERY = "UPDATE courseware.feedback_by_interactive " +
                "SET feedback_ids = feedback_ids + ?" +
                "WHERE interactive_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Lists.newArrayList(feedbackId), interactiveId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement removeFeedback(UUID feedbackId, UUID interactiveId) {
        final String QUERY = "UPDATE courseware.feedback_by_interactive " +
                "SET feedback_ids = feedback_ids - ?" +
                "WHERE interactive_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Lists.newArrayList(feedbackId), interactiveId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
