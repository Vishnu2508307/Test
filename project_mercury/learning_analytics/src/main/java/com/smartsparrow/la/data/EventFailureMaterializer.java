package com.smartsparrow.la.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class EventFailureMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    EventFailureMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findByEvent(UUID eventId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  event_id"
                + ", fail_id"
                + ", exception_message"
                + " FROM analytics_event.failure_by_event"
                + " WHERE event_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(eventId);
        return stmt;
    }

    public Statement findByEventIdAndFailId(UUID eventId, UUID failId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  event_id"
                + ", fail_id"
                + ", exception_message"
                + " FROM analytics_event.failure_by_event"
                + " WHERE event_id = ? and fail_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(eventId);
        stmt.bind(failId);
        return stmt;
    }
}
