package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LearnerDocumentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerDocumentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(UUID documentId) {
        String SELECT = "SELECT" +
                " id" +
                ", title" +
                ", created_at" +
                ", created_by" +
                ", modified_at" +
                ", modified_by" +
                ", document_version_id" +
                ", origin" +
                " FROM learner.document" +
                " WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(documentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerDocument fromRow(Row row) {
        return new LearnerDocument()
                .setId(row.getUUID("id"))
                .setTitle(row.getString("title"))
                .setCreatedAt(row.getUUID("created_at"))
                .setCreatedBy(row.getUUID("created_by"))
                .setModifiedAt(row.getUUID("modified_at"))
                .setModifiedBy(row.getUUID("modified_by"))
                .setDocumentVersionId(row.getUUID("document_version_id"))
                .setOrigin(row.getString("origin"));
    }
}
