package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ChildWalkableByLearnerPathwayMutator extends SimpleTableMutator<LearnerWalkablePathwayChildren> {

    public Statement addWalkable(LearnerWalkablePathwayChildren child) {
        final String ADD_WALKABLE = "UPDATE learner.child_walkable_by_pathway " +
                "SET walkable_ids = walkable_ids + ?, " +
                "walkable_types = walkable_types + ? " +
                "WHERE pathway_id = ? " +
                "AND deployment_id = ? " +
                "AND change_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_WALKABLE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                child.getWalkableIds(),
                child.getWalkableTypes(),
                child.getPathwayId(),
                child.getDeploymentId(),
                child.getChangeId()
        );
        stmt.setIdempotent(false);
        return stmt;
    }
}
