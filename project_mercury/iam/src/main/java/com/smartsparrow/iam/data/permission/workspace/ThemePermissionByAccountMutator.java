package com.smartsparrow.iam.data.permission.workspace;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ThemePermissionByAccountMutator extends SimpleTableMutator<ThemePermissionByAccount> {

    @Override
    public String getUpsertQuery(ThemePermissionByAccount mutation) {
        return "INSERT INTO iam_global.theme_permission_by_account (" +
                "account_id, " +
                "theme_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(ThemePermissionByAccount mutation) {
        return "DELETE FROM iam_global.theme_permission_by_account " +
                "WHERE account_id = ? " +
                "AND theme_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ThemePermissionByAccount mutation) {
        stmt.bind(mutation.getAccountId(),
                  mutation.getThemeId(),
                  mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, ThemePermissionByAccount mutation) {
        stmt.bind(mutation.getAccountId(),
                  mutation.getThemeId());
    }
}
