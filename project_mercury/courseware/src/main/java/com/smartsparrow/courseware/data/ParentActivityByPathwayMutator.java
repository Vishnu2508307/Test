package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentActivityByPathwayMutator extends SimpleTableMutator<UUID> {

    public Statement insert(UUID pathwayId, UUID activityId) {
        final String INSERT = "INSERT INTO courseware.parent_activity_by_pathway (" +
                "pathway_id, " +
                "activity_id) " +
                "VALUES (?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(INSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pathwayId, activityId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement delete(UUID pathwayId) {
        final String DELETE = "DELETE FROM courseware.parent_activity_by_pathway " +
                "WHERE pathway_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(DELETE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pathwayId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
