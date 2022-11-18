package com.smartsparrow.iam.data.permission.plugin;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class PluginPermissionByAccountMutator extends SimpleTableMutator<AccountPluginPermission> {

    @Override
    public String getUpsertQuery(AccountPluginPermission mutation) {
        return "INSERT INTO iam_global.plugin_permission_by_account ("
                + "  account_id"
                + ", plugin_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountPluginPermission mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getPluginId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(AccountPluginPermission mutation) {
        return "DELETE FROM iam_global.plugin_permission_by_account WHERE " +
                "account_id = ? " +
                "AND plugin_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountPluginPermission mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getPluginId());
    }
}
