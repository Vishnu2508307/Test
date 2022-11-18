package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncStatus;

import javax.inject.Inject;
import java.util.UUID;

class PublicationSummaryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PublicationSummaryMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAll() {

        final String QUERY = "SELECT * FROM publication.summary";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findById(final UUID publicationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  id" +
                ", title" +
                ", description" +
                ", config" +
                ", output_type" +
                " FROM publication.summary" +
                " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(publicationId);
        return stmt;
    }

    public PublicationSummary fromRow(Row row) {
        return new PublicationSummary()
                .setId(row.getUUID("id"))
                .setTitle(row.getString("title"))
                .setDescription(row.getString("description"))
                .setConfig(row.getString("config"))
                .setOutputType(row.isNull("output_type") ? PublicationOutputType.EPUB_ETEXT : Enums.of(PublicationOutputType.class, row.getString("output_type")));
    }
}
