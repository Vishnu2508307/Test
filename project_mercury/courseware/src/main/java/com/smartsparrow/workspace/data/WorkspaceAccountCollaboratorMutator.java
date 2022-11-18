package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspaceAccountCollaboratorMutator extends SimpleTableMutator<WorkspaceAccountCollaborator> {


    @Override
    public String getUpsertQuery(WorkspaceAccountCollaborator mutation) {
        // @formatter:off
        return "INSERT INTO workspace.account_by_workspace("
                + " workspace_id"
                + ",  account_id"
                + ",  permission_level"
                + ") VALUES ( ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public String getDeleteQuery(WorkspaceAccountCollaborator mutation) {
        return "DELETE FROM workspace.account_by_workspace " +
                "WHERE workspace_id = ? " +
                "AND account_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, WorkspaceAccountCollaborator mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getAccountId(), mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, WorkspaceAccountCollaborator mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getAccountId());
    }
}
