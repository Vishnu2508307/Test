package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;
import java.util.UUID;

class PublicationMetadataByPublishedActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PublicationMetadataByPublishedActivityMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID activityId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  activity_id" +
                ", version" +
                ", publication_id" +
                ", author" +
                ", etext_version" +
                ", book_id" +
                ", created_by" +
                ", created_at" +
                ", updated_by" +
                ", updated_at" +
                " FROM publication.metadata_by_published_activity" +
                " WHERE activity_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public Statement findByIdAndVersion(final UUID activityId, final String version) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  activity_id" +
                ", version" +
                ", publication_id" +
                ", author" +
                ", etext_version" +
                ", book_id" +
                ", created_by" +
                ", created_at" +
                ", updated_by" +
                ", updated_at" +
                " FROM publication.metadata_by_published_activity" +
                " WHERE activity_id = ?" +
                " AND version = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId, version);
        return stmt;
    }

    public PublicationMetadataByPublishedActivity fromRow(Row row) {
        return (PublicationMetadataByPublishedActivity) new PublicationMetadataByPublishedActivity()
                .setActivityId(row.getUUID("activity_id"))
                .setVersion(row.getString("version"))
                .setPublicationId(row.getUUID("publication_id"))
                .setAuthor(row.getString("author"))
                .setEtextVersion(row.getString("etext_version"))
                .setBookId(row.getString("book_id"))
                .setCreatedBy(row.getUUID("created_by"))
                .setCreatedAt(row.getUUID("created_at"))
                .setUpdatedBy(row.getUUID("updated_by"))
                .setUpdatedAt(row.getUUID("updated_at"));
    }
}
