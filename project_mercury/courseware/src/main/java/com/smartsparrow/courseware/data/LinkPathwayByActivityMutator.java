package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LinkPathwayByActivityMutator extends SimpleTableMutator<UUID> {

    public Statement insert(UUID activityId, UUID pathwayId) {
        final String UPSERT = "INSERT INTO courseware.link_pathway_by_activity (" +
                "activity_id, " +
                "pathway_id) " +
                "VALUES (?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(activityId, pathwayId);
        stmt.setIdempotent(true);
        return stmt;    }

    public Statement delete(UUID activityId, UUID pathwayId) {
        String DELETE = "DELETE FROM courseware.link_pathway_by_activity " +
                "WHERE activity_id = ? " +
                "AND pathway_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(DELETE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(activityId, pathwayId);
        stmt.setIdempotent(true);
        return stmt;    }
}
