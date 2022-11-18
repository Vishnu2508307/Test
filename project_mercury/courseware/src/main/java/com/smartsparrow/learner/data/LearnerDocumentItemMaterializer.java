package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LearnerDocumentItemMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerDocumentItemMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(UUID documentItemId) {
        String SELECT = "SELECT" +
                " id" +
                ", document_id" +
                ", full_statement" +
                ", abbreviated_statement" +
                ", human_coding_scheme" +
                ", created_by" +
                ", created_at" +
                ", modified_by" +
                ", modified_at" +
                " FROM learner.document_item" +
                " WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(documentItemId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerDocumentItem fromRow(Row row) {
        return new LearnerDocumentItem()
                .setId(row.getUUID("id"))
                .setDocumentId(row.getUUID("document_id"))
                .setFullStatement(row.getString("full_statement"))
                .setAbbreviatedStatement(row.getString("abbreviated_statement"))
                .setHumanCodingScheme(row.getString("human_coding_scheme"))
                .setCreatedAt(row.getUUID("created_at"))
                .setCreatedBy(row.getUUID("created_by"))
                .setModifiedAt(row.getUUID("modified_at"))
                .setModifiedBy(row.getUUID("modified_by"));
    }
}
