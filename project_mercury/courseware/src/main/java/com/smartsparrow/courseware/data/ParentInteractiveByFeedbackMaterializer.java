package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ParentInteractiveByFeedbackMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public ParentInteractiveByFeedbackMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchById(final UUID feedbackId) {
        // @formatter:off
        final String QUERY = "SELECT "
                             + " interactive_id"
                             + " FROM courseware.parent_interactive_by_feedback"
                             + " WHERE feedback_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(feedbackId);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("interactive_id");
    }
}
