package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DocumentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchById(UUID id) {
        String BY_ID = "SELECT id, " +
                "title, " +
                "created_at," +
                "created_by, " +
                "modified_at, " +
                "modified_by, " +
                "workspace_id, " +
                "origin FROM competency.document " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Document fromRow(Row row) {
        return new Document()
                .setId(row.getUUID("id"))
                .setTitle(row.getString("title"))
                .setCreatedAt(row.getUUID("created_at"))
                .setCreatedBy(row.getUUID("created_by"))
                .setModifiedAt(row.getUUID("modified_at"))
                .setModifiedBy(row.getUUID("modified_by"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setOrigin(row.getString("origin"));
    }
}
