package com.smartsparrow.courseware.data;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class FeedbackByInteractiveMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public FeedbackByInteractiveMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchAll(final UUID interactiveId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " feedback_ids"
                + " FROM courseware.feedback_by_interactive"
                + " WHERE interactive_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(interactiveId);
        return stmt;
    }

    public List<UUID> fromRow(Row row) {
        return row.getList("feedback_ids", UUID.class);
    }
}
