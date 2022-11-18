package com.smartsparrow.la.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class EventSummaryMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    EventSummaryMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findById(UUID id) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", namespace"
                + ", message_type_code"
                + ", version"
                + ", stream_type"
                + ", correlation_id"
                + ", payload"
                + ", tags"
                + " FROM analytics_event.event_summary"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(id);
        return stmt;
    }

}
