package com.smartsparrow.iam.data.permission.workspace;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspacePermissionByAccountMutator extends SimpleTableMutator<AccountWorkspacePermission> {

    @Override
    public String getUpsertQuery(AccountWorkspacePermission mutation) {
        return "INSERT INTO iam_global.workspace_permission_by_account (" +
                "account_id, " +
                "workspace_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(AccountWorkspacePermission mutation) {
        return "DELETE FROM iam_global.workspace_permission_by_account " +
                "WHERE account_id = ? " +
                "AND workspace_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountWorkspacePermission mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getWorkspaceId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountWorkspacePermission mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getWorkspaceId());
    }
}
