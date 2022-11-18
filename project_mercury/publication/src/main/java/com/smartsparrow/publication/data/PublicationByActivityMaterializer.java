package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;
import java.util.UUID;

class PublicationByActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PublicationByActivityMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID activityId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  publication_id" +
                ", activity_id" +
                " FROM publication.publication_by_activity" +
                " WHERE activity_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public PublicationByActivity fromRow(Row row) {
        return new PublicationByActivity()
                .setPublicationId(row.getUUID("publication_id"))
                .setActivityId(row.getUUID("activity_id"));
    }
}
