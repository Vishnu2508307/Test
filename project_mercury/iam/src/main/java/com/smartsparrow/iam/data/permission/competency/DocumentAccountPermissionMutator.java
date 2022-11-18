package com.smartsparrow.iam.data.permission.competency;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentAccountPermissionMutator extends SimpleTableMutator<AccountDocumentPermission> {

    @Override
    public String getUpsertQuery(AccountDocumentPermission mutation) {
        return "INSERT INTO iam_global.document_permission_by_account (" +
                "account_id, " +
                "document_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(AccountDocumentPermission mutation) {
        return "DELETE FROM iam_global.document_permission_by_account " +
                "WHERE account_id = ? " +
                "AND document_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountDocumentPermission mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getDocumentId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountDocumentPermission mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getDocumentId());
    }
}
