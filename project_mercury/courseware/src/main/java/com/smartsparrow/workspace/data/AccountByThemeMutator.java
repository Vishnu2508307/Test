package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class AccountByThemeMutator extends SimpleTableMutator<AccountByTheme> {


    @Override
    public String getUpsertQuery(AccountByTheme mutation) {
        // @formatter:off
        return "INSERT INTO workspace.account_by_theme("
                + "   theme_id"
                + ",  account_id"
                + ",  permission_level"
                + ") VALUES ( ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public String getDeleteQuery(AccountByTheme mutation) {
        return "DELETE FROM workspace.account_by_theme " +
                "WHERE theme_id = ? " +
                "AND account_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountByTheme mutation) {
        stmt.bind(mutation.getThemeId(), mutation.getAccountId(), mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountByTheme mutation) {
        stmt.bind(mutation.getThemeId(), mutation.getAccountId());
    }
}
