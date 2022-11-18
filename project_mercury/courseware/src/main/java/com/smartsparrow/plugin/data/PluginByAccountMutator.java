package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class PluginByAccountMutator extends SimpleTableMutator<PluginAccount> {

    @Override
    public String getUpsertQuery(PluginAccount mutation) {
        return "INSERT INTO workspace.plugin_by_account ("
                + "  account_id"
                + ", plugin_id"
                + ") VALUES ( ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getPluginId());
    }

    @Override
    public String getDeleteQuery(PluginAccount mutation) {
        return "DELETE FROM workspace.plugin_by_account WHERE " +
                "account_id = ? " +
                "AND plugin_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, PluginAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getPluginId());
    }
}
