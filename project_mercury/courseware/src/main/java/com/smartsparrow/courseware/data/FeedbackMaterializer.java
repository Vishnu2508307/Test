package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class FeedbackMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    FeedbackMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchById(final UUID feedbackId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", plugin_id"
                + ", plugin_version_expr"
                + " FROM courseware.feedback"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(feedbackId);
        return stmt;
    }

}
