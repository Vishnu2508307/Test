package com.smartsparrow.competency.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentVersionMutator extends SimpleTableMutator<DocumentVersion> {

    private static final String UPSERT = "INSERT INTO competency.document_version (" +
            "document_id, " +
            "version_id, " +
            "author_id) VALUES (?,?,?)";

    @Override
    public String getUpsertQuery(DocumentVersion mutation) {
        return UPSERT;
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DocumentVersion mutation) {
        stmt.bind(mutation.getDocumentId(),
                mutation.getVersionId(),
                mutation.getAuthorId());
    }

    public Statement updateVersion(UUID documentId, UUID authorId, UUID versionId) {
        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(documentId, versionId, authorId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
