package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;
import java.util.UUID;

class PublicationMetadataMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PublicationMetadataMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID publicationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  publication_id" +
                ", author" +
                ", etext_version" +
                ", book_id" +
                ", created_by" +
                ", created_at" +
                ", updated_by" +
                ", updated_at" +
                " FROM publication.metadata_by_publication" +
                " WHERE publication_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(publicationId);
        return stmt;
    }

    public PublicationMetadata fromRow(Row row) {
        return new PublicationMetadata()
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
