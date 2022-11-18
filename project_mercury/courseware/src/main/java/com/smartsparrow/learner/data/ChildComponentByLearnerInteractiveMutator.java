package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ChildComponentByLearnerInteractiveMutator extends SimpleTableMutator<LearnerChildComponent> {

    public Statement addChild(UUID interactiveId, UUID deploymentId, UUID changeId, UUID componentId) {
        final String ADD_CHILD = "UPDATE learner.child_component_by_interactive " +
                "SET component_ids = component_ids + ? " +
                "WHERE interactive_id = ? " +
                "AND deployment_id = ? " +
                "AND change_id = ? ";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_CHILD);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                Lists.newArrayList(componentId),
                interactiveId,
                deploymentId,
                changeId
        );
        stmt.setIdempotent(false);
        return stmt;
    }
}
