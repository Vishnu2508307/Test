package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;
import java.util.UUID;

class PublicationByExportMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PublicationByExportMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID exportId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  publication_id" +
                ", export_id" +
                " FROM publication.publication_by_export" +
                " WHERE export_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(exportId);
        return stmt;
    }

    public PublicationByExport fromRow(Row row) {
        return new PublicationByExport()
                .setPublicationId(row.getUUID("publication_id"))
                .setExportId(row.getUUID("export_id"));
    }
}
