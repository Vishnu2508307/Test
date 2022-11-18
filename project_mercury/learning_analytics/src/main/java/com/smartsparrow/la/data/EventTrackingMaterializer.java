package com.smartsparrow.la.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class EventTrackingMaterializer implements TableMaterializer {
    private final PreparedStatementCache preparedStatementCache;

    @Inject
    EventTrackingMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findByEventId(UUID eventId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  event_id"
                + ", tracking_id"
                + " FROM analytics_event.tracking_by_event"
                + " WHERE event_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(eventId);
        return stmt;
    }
}
