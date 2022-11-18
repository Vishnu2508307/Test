package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerDocumentItemByDocumentMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID documentId, UUID documentItemId) {
        String upsertQuery = "INSERT INTO learner.document_item_by_document (" +
                "document_id, " +
                "document_item_id) VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(upsertQuery);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(documentId, documentItemId);

        return stmt;
    }
}
