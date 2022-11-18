package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class WorkspaceBySubscriptionMutator extends SimpleTableMutator<Workspace> {

    @Override
    public String getUpsertQuery(Workspace mutation) {
        // @formatter:off
        return "INSERT INTO workspace.workspace_by_subscription ("
                + "  subscription_id"
                + ", workspace_id"
                + ") VALUES ( ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Workspace mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getId());
    }

    @Override
    public String getDeleteQuery(Workspace mutation) {
        return "DELETE FROM workspace.workspace_by_subscription " +
                "WHERE subscription_id = ? " +
                "AND workspace_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, Workspace mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getId());
    }

}
