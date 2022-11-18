package com.smartsparrow.competency.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ItemAssociationByDocumentMutator extends SimpleTableMutator<ItemAssociation> {

    @Override
    public String getUpsertQuery(ItemAssociation mutation) {
        return "INSERT INTO competency.item_association_by_document (" +
                "document_id, " +
                "association_id) VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ItemAssociation mutation) {
        stmt.bind(mutation.getDocumentId(), mutation.getId());
    }

    @Override
    public String getDeleteQuery(ItemAssociation mutation) {
        return "DELETE FROM competency.item_association_by_document " +
                "WHERE document_id = ? AND association_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ItemAssociation mutation) {
        stmt.bind(mutation.getDocumentId(), mutation.getId());
    }

    public Statement upsert(UUID documentId, UUID associationId) {
        String upsertQuery = "INSERT INTO competency.item_association_by_document (" +
                "document_id, " +
                "association_id) VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(upsertQuery);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(documentId, associationId);

        return stmt;
    }

    public Statement delete(UUID documentId, UUID associationId) {
        String deleteQuery = "DELETE FROM competency.item_association_by_document " +
                "WHERE document_id = ? AND association_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(deleteQuery);
        stmt.setConsistencyLevel(deleteConsistencyLevel());
        stmt.setIdempotent(isDeleteIdempotent());
        stmt.bind(documentId, associationId);
        return stmt;
    }
}
