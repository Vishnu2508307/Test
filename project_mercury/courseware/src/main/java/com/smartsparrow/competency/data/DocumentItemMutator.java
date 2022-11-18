package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentItemMutator extends SimpleTableMutator<DocumentItem> {

    @Override
    public String getUpsertQuery(DocumentItem mutation) {
        return "INSERT INTO competency.item (" +
                "id, " +
                "document_id, " +
                "full_statement, " +
                "abbreviated_statement, " +
                "human_coding_scheme, " +
                "created_at, " +
                "created_by, " +
                "modified_at, " +
                "modified_by) VALUES (?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DocumentItem mutation) {
        stmt.bind(mutation.getId(),
                mutation.getDocumentId(),
                mutation.getFullStatement(),
                mutation.getAbbreviatedStatement(),
                mutation.getHumanCodingScheme(),
                mutation.getCreatedAt(),
                mutation.getCreatedById(),
                mutation.getModifiedAt(),
                mutation.getModifiedById());
    }

    @Override
    public String getDeleteQuery(DocumentItem mutation) {
        return "DELETE FROM competency.item " +
                "WHERE id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, DocumentItem mutation) {
        stmt.bind(mutation.getId());
    }

    public Statement updateFields(DocumentItem documentItem) {
        // @formatter:off
        final String UPDATE_ITEM = "UPDATE competency.item SET"
                + " full_statement = ?,"
                + " abbreviated_statement = ?,"
                + " human_coding_scheme = ?,"
                + " modified_at = ?,"
                + " modified_by = ?"
                + " where id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE_ITEM);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                documentItem.getFullStatement(),
                documentItem.getAbbreviatedStatement(),
                documentItem.getHumanCodingScheme(),
                documentItem.getModifiedAt(),
                documentItem.getModifiedById(),
                documentItem.getId()
        );
        stmt.setIdempotent(true);
        return stmt;
    }
}
