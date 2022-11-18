package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AccountDocumentCollaboratorMutator extends SimpleTableMutator<AccountDocumentCollaborator> {

    @Override
    public String getUpsertQuery(AccountDocumentCollaborator mutation) {
        return "INSERT INTO competency.account_by_document (" +
                "document_id, " +
                "account_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountDocumentCollaborator mutation) {
        stmt.bind(mutation.getDocumentId(),
                mutation.getAccountId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(AccountDocumentCollaborator mutation) {
        return "DELETE FROM competency.account_by_document " +
                "WHERE document_id = ? " +
                "AND account_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountDocumentCollaborator mutation) {
        stmt.bind(mutation.getDocumentId(),
                mutation.getAccountId());
    }
}
