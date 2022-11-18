package com.smartsparrow.competency.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ItemByDocumentMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID documentId, UUID itemId) {
        String upsertQuery = "INSERT INTO competency.item_by_document (" +
                "document_id, " +
                "item_id) VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(upsertQuery);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(documentId, itemId);

        return stmt;
    }

    public Statement delete(UUID documentId, UUID itemId) {
        String deleteQuery = "DELETE FROM competency.item_by_document " +
                "WHERE document_id = ? AND item_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(deleteQuery);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(documentId, itemId);
        return stmt;
    }
}
