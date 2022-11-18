package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspaceSummaryMutator extends SimpleTableMutator<Workspace> {

    @Override
    public String getUpsertQuery(Workspace mutation) {
        // @formatter:off
        return "INSERT INTO workspace.summary("
                + "  id"
                + ", subscription_id"
                + ", name"
                + ", description"
                + ") VALUES ( ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Workspace mutation) {
        stmt.bind(mutation.getId(), mutation.getSubscriptionId(), mutation.getName(), mutation.getDescription());
    }

}
