package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentByWorkspaceMutator extends SimpleTableMutator<Document> {

    @Override
    public String getUpsertQuery(Document mutation) {
        return "INSERT INTO competency.document_by_workspace (" +
                "workspace_id, " +
                "document_id) VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Document mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getId());
    }

    @Override
    public String getDeleteQuery(Document mutation) {
        return "DELETE FROM competency.document_by_workspace " +
                "WHERE workspace_id = ? AND document_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, Document mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getId());
    }
}
