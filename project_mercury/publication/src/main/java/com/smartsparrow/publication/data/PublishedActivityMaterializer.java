package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

import javax.inject.Inject;
import java.util.UUID;

class PublishedActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PublishedActivityMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAll() {

        final String QUERY = "SELECT * FROM publication.published_activity";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findById(final UUID activityId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  activity_id" +
                ", version" +
                ", description" +
                ", publication_id" +
                ", title" +
                ", output_type" +
                " FROM publication.published_activity" +
                " WHERE activity_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public PublishedActivity fromRow(Row row) {
        return new PublishedActivity()
                .setActivityId(row.getUUID("activity_id"))
                .setVersion(row.getString("version"))
                .setDescription(row.getString("description"))
                .setPublicationId(row.getUUID("publication_id"))
                .setTitle(row.getString("title"))
                .setOutputType(row.isNull("output_type") ? PublicationOutputType.EPUB_ETEXT : Enums.of(PublicationOutputType.class, row.getString("output_type")));
    }
}
