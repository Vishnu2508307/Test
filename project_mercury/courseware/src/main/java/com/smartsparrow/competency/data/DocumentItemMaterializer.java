package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DocumentItemMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentItemMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchById(UUID id) {
        String BY_ID = "SELECT id, " +
                "document_id, " +
                "full_statement, " +
                "abbreviated_statement, " +
                "human_coding_scheme, " +
                "created_at, " +
                "created_by, " +
                "modified_at, " +
                "modified_by FROM competency.item " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public DocumentItem fromRow(Row row) {
        return new DocumentItem()
                .setId(row.getUUID("id"))
                .setDocumentId(row.getUUID("document_id"))
                .setFullStatement(row.getString("full_statement"))
                .setAbbreviatedStatement(row.getString("abbreviated_statement"))
                .setHumanCodingScheme(row.getString("human_coding_scheme"))
                .setCreatedAt(row.getUUID("created_at"))
                .setCreatedById(row.getUUID("created_by"))
                .setModifiedAt(row.getUUID("modified_at"))
                .setModifiedById(row.getUUID("modified_by"));
    }
}
