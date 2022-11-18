package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentByAccountMutator extends SimpleTableMutator<DocumentAccount> {

    @Override
    public String getUpsertQuery(DocumentAccount mutation) {
        return "INSERT INTO competency.document_by_account (" +
                "account_id, " +
                "document_id) VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DocumentAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getDocumentId());
    }

    @Override
    public String getDeleteQuery(DocumentAccount mutation) {
        return "DELETE FROM competency.document_by_account " +
                "WHERE account_id = ? " +
                "AND document_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, DocumentAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getDocumentId());
    }
}
