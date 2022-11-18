package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ActivityByWorkspaceMutator extends SimpleTableMutator<ActivityByWorkspace> {

    @Override
    public String getUpsertQuery(ActivityByWorkspace mutation) {
        // @formatter:off
        return "INSERT INTO workspace.activity_by_workspace ("
                + "  workspace_id"
                + ", activity_id"
                + ") VALUES ( ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ActivityByWorkspace mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getActivityId());
    }

    @Override
    public String getDeleteQuery(ActivityByWorkspace mutation) {
        // @formatter:off
        return "DELETE FROM workspace.activity_by_workspace "
                + " WHERE workspace_id = ? AND activity_id = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, ActivityByWorkspace mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getActivityId());
    }
}
