package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ChildComponentByLearnerActivityMutator extends SimpleTableMutator<LearnerChildComponent> {

    public Statement addChild(UUID activityId, UUID deploymentId, UUID changeId, UUID componentId) {
        final String ADD_CHILD = "UPDATE learner.child_component_by_activity " +
                "SET component_ids = component_ids + ? " +
                "WHERE activity_id = ? " +
                "AND deployment_id = ? " +
                "AND change_id = ? ";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_CHILD);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                Lists.newArrayList(componentId),
                activityId,
                deploymentId,
                changeId
        );
        stmt.setIdempotent(false);
        return stmt;
    }
}
