package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ChildPathwayByLearnerActivityMutator extends SimpleTableMutator<LearnerChildPathway> {

    public Statement addChild(UUID activityId, UUID deploymentId, UUID changeId, UUID pathwayId) {
        final String ADD_CHILD = "UPDATE learner.child_pathway_by_activity " +
                "SET pathway_ids = pathway_ids + ? " +
                "WHERE activity_id = ? " +
                "AND deployment_id = ? " +
                "AND change_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_CHILD);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                Lists.newArrayList(pathwayId),
                activityId,
                deploymentId,
                changeId
        );
        stmt.setIdempotent(false);
        return stmt;
    }
}
