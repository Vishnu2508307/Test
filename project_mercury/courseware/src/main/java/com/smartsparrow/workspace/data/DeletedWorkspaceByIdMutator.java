package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DeletedWorkspaceByIdMutator extends SimpleTableMutator<DeletedWorkspace> {

    @Override
    public String getUpsertQuery(DeletedWorkspace mutation) {
        // @formatter:off
        return "INSERT INTO workspace.deleted_workspace_by_id ("
                + "  workspace_id"
                + ", name"
                + ", account_id"
                + ", deleted_at"
                + ") VALUES ( ?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeletedWorkspace mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getName(), mutation.getAccountId(), mutation.getDeletedAt());
    }
}
