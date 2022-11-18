package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AccountByPluginMutator extends SimpleTableMutator<PluginAccountCollaborator> {

    @Override
    public String getUpsertQuery(PluginAccountCollaborator mutation) {
        return "INSERT INTO workspace.account_by_plugin ("
                + "  plugin_id"
                + ", account_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, PluginAccountCollaborator mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getAccountId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(PluginAccountCollaborator mutation) {
        String deleteAll = "DELETE FROM workspace.account_by_plugin WHERE " +
                "plugin_id = ?";

        if(mutation.getAccountId() == null) {
            return deleteAll;
        } else {
            return deleteAll + " AND account_id = ?";
        }
    }

    @Override
    public void bindDelete(BoundStatement stmt, PluginAccountCollaborator mutation) {
        if(mutation.getAccountId() == null) {
            stmt.bind(mutation.getPluginId());
        } else {
            stmt.bind(mutation.getPluginId(), mutation.getAccountId());
        }
    }
}
