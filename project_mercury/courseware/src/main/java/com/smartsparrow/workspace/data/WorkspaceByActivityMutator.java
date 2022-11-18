package com.smartsparrow.workspace.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspaceByActivityMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID activityId, UUID workspaceId) {
        // @formatter:off
        String QUERY = "INSERT INTO workspace.workspace_by_activity ("
                        + "  activity_id"
                        + ", workspace_id"
                        + ") VALUES ( ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(upsertConsistencyLevel());
        stmt.setIdempotent(isUpsertIdempotent());
        stmt.bind(activityId, workspaceId);
        return stmt;
    }

    public Statement delete(UUID activityId) {
        // @formatter:off
        String QUERY = "DELETE FROM workspace.workspace_by_activity"
                        + " WHERE activity_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(upsertConsistencyLevel());
        stmt.setIdempotent(isUpsertIdempotent());
        stmt.bind(activityId);
        return stmt;
    }

}
