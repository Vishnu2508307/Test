package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class FeedbackByLearnerInteractiveMutator extends SimpleTableMutator<FeedbackByLearnerInteractive> {

    public Statement addChild(UUID feedbackId, UUID parentInteractiveId, UUID deploymentId, UUID changeId) {
        final String ADD_CHILD = "UPDATE learner.feedback_by_interactive " +
                "SET feedback_ids = feedback_ids + ? " +
                "WHERE interactive_id = ? " +
                "AND deployment_id = ? " +
                "AND change_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_CHILD);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                Lists.newArrayList(feedbackId),
                parentInteractiveId,
                deploymentId,
                changeId
        );
        stmt.setIdempotent(false);
        return stmt;
    }
}
