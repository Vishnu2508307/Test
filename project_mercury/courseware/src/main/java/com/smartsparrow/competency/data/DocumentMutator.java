package com.smartsparrow.competency.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentMutator extends SimpleTableMutator<Document> {

    @Override
    public String getUpsertQuery(Document mutation) {
        return "INSERT INTO competency.document (" +
                "id, " +
                "title, " +
                "created_at, " +
                "created_by, " +
                "modified_at, " +
                "modified_by, " +
                "workspace_id, " +
                "origin) VALUES (?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Document mutation) {
        stmt.bind(mutation.getId(),
                mutation.getTitle(),
                mutation.getCreatedAt(),
                mutation.getCreatedBy(),
                mutation.getModifiedAt(),
                mutation.getModifiedBy(),
                mutation.getWorkspaceId(),
                mutation.getOrigin());
    }

    @Override
    public String getDeleteQuery(Document mutation) {
        return "DELETE FROM competency.document " +
                "WHERE id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, Document mutation) {
        stmt.bind(mutation.getId());
    }

    public Statement updateDocumentEdit(UUID documentId, UUID accountId, UUID time) {
        // @formatter:off
        final String UPDATE_MODIFIED = "UPDATE competency.document SET"
                + " modified_at = ?,"
                + " modified_by = ?"
                + " where id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE_MODIFIED);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(time, accountId, documentId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
