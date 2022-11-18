package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DocumentVersionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentVersionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchLatest(UUID documentId) {
        String BY_ID = "SELECT document_id, " +
                "version_id, " +
                "author_id FROM competency.document_version " +
                "WHERE document_id = ? LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(documentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public DocumentVersion fromRow(Row row) {
        return new DocumentVersion()
                .setDocumentId(row.getUUID("document_id"))
                .setVersionId(row.getUUID("version_id"))
                .setAuthorId(row.getUUID("author_id"));
    }
}
