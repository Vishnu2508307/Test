package com.smartsparrow.courseware.data;

import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ChildPathwayByActivityMutator extends SimpleTableMutator<UUID> {

    public Statement addPathway(UUID pathwayId, UUID activityId) {
        final String ADD_PATHWAY = "UPDATE courseware.child_pathway_by_activity " +
                "SET pathway_ids = pathway_ids + ?" +
                "WHERE activity_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_PATHWAY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Lists.newArrayList(pathwayId), activityId);
        stmt.setIdempotent(false);
        return stmt;
    }

    public Statement removePathway(UUID pathwayId, UUID activityId) {
        final String REMOVE_PATHWAY = "UPDATE courseware.child_pathway_by_activity " +
                "SET pathway_ids = pathway_ids - ?" +
                "WHERE activity_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE_PATHWAY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Lists.newArrayList(pathwayId), activityId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement insert(UUID activityId, List<UUID> pathwayIds) {
        final String UPSERT = "INSERT INTO courseware.child_pathway_by_activity (" +
                "activity_id, " +
                "pathway_ids) " +
                "VALUES (?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(activityId, pathwayIds);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement deleteAllBy(UUID activityId) {
        String DELETE = "DELETE FROM courseware.child_pathway_by_activity " +
                "WHERE activity_id = ? ";

        BoundStatement stmt = stmtCache.asBoundStatement(DELETE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(activityId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
