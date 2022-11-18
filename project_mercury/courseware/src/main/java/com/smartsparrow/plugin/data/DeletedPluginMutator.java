package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DeletedPluginMutator extends SimpleTableMutator<DeletedPlugin> {

    @Override
    public String getUpsertQuery(DeletedPlugin mutation) {
        return "INSERT INTO plugin.deleted (" +
                "id, " +
                "plugin_id, " +
                "account_id) VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeletedPlugin mutation) {
        stmt.bind(mutation.getId(), mutation.getPluginId(), mutation.getAccountId());
    }
}
