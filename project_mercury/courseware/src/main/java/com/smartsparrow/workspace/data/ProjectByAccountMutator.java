package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ProjectByAccountMutator extends SimpleTableMutator<ProjectAccount> {

    @Override
    public String getUpsertQuery(final ProjectAccount mutation) {
        return "INSERT INTO workspace.project_by_account_workspace (" +
                "account_id" +
                ", workspace_id" +
                ", project_id" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ProjectAccount mutation) {
        stmt.bind(
                mutation.getAccountId(),
                mutation.getWorkspaceId(),
                mutation.getProjectId()
        );
    }

    @Override
    public String getDeleteQuery(final ProjectAccount mutation) {
        return "DELETE FROM workspace.project_by_account_workspace" +
                " WHERE account_id = ?" +
                " AND workspace_id = ?" +
                " AND project_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final ProjectAccount mutation) {
        stmt.bind(
                mutation.getAccountId(),
                mutation.getWorkspaceId(),
                mutation.getProjectId()
        );
    }
}
