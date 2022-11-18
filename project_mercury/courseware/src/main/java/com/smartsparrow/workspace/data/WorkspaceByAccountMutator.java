package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspaceByAccountMutator extends SimpleTableMutator<WorkspaceAccount> {

    @Override
    public String getUpsertQuery(WorkspaceAccount mutation) {
        // @formatter:off
        return "INSERT INTO workspace.workspace_by_account ("
                + "  account_id"
                + ", workspace_id"
                + ") VALUES ( ?, ?)";
        // @formatter:on
    }

    @Override
    public String getDeleteQuery(WorkspaceAccount mutation) {
        return "DELETE FROM workspace.workspace_by_account " +
                "WHERE account_id = ? " +
                "AND workspace_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, WorkspaceAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getWorkspaceId());
    }

    @Override
    public void bindDelete(BoundStatement stmt, WorkspaceAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getWorkspaceId());
    }
}
