package com.smartsparrow.publication.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class PublishedActivityByPublicationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PublishedActivityByPublicationMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID publicationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  publication_id" +
                ", activity_id" +
                ", version" +
                ", description" +
                ", title" +
                ", output_type" +
                ", status" +
                " FROM publication.activity_by_publication" +
                " WHERE publication_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(publicationId);
        return stmt;
    }

    public Statement findByActivityId(final UUID activityId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  publication_id" +
                ", activity_id" +
                ", version" +
                ", description" +
                ", title" +
                ", output_type" +
                ", status" +
                " FROM publication.activity_by_publication" +
                " WHERE activity_id = ? ALLOW FILTERING";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public Statement updateActivityPublicationStatus(final UUID publicationId, final ActivityPublicationStatus activityPublicationStatus, final  UUID activityId, final String version) {
        final String UPDATE = "UPDATE publication.activity_by_publication "
                + "SET status = ? "
                + "WHERE publication_id = ? AND activity_id = ? AND version = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Enums.asString(activityPublicationStatus),
                  publicationId, activityId, version);
        stmt.setIdempotent(true);
        return stmt;
    }

    public PublishedActivity fromRow(Row row) {
        return new PublishedActivity()
                .setPublicationId(row.getUUID("publication_id"))
                .setActivityId(row.getUUID("activity_id"))
                .setVersion(row.getString("version"))
                .setDescription(row.getString("description"))
                .setTitle(row.getString("title"))
                .setOutputType(row.isNull("output_type") ? PublicationOutputType.EPUB_ETEXT : Enums.of(PublicationOutputType.class, row.getString("output_type")))
                .setStatus(row.isNull("status") ? ActivityPublicationStatus.ACTIVE : Enums.of(ActivityPublicationStatus.class, row.getString("status")));
    }
}
