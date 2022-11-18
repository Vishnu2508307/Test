package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ThemeByAccountMutator extends SimpleTableMutator<ThemeByAccount> {

    @Override
    public String getUpsertQuery(ThemeByAccount mutation) {
        // @formatter:off
        return "INSERT INTO workspace.theme_by_account ("
                + "  account_id"
                + ", theme_id"
                + ") VALUES ( ?, ?)";
        // @formatter:on
    }

    @Override
    public String getDeleteQuery(ThemeByAccount mutation) {
        return "DELETE FROM workspace.theme_by_account " +
                "WHERE account_id = ? " +
                "AND theme_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ThemeByAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getThemeId());
    }

    @Override
    public void bindDelete(BoundStatement stmt, ThemeByAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getThemeId());
    }
}
