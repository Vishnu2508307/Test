package com.smartsparrow.iam.data.permission.workspace;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class AccountProjectPermissionMutator extends SimpleTableMutator<AccountProjectPermission> {

    @Override
    public String getUpsertQuery(final AccountProjectPermission mutation) {
        return "INSERT INTO iam_global.project_permission_by_account (" +
                "account_id" +
                ", project_id" +
                ", permission_level" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final AccountProjectPermission mutation) {
        stmt.bind(
                mutation.getAccountId(),
                mutation.getProjectId(),
                Enums.asString(mutation.getPermissionLevel())
        );
    }

    @Override
    public String getDeleteQuery(final AccountProjectPermission mutation) {
        return "DELETE FROM iam_global.project_permission_by_account" +
                " WHERE account_id = ?" +
                " AND project_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final AccountProjectPermission mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getProjectId());
    }
}
