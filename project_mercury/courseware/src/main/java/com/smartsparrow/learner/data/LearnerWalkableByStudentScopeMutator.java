package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerWalkableByStudentScopeMutator extends SimpleTableMutator<UUID> {

    @SuppressWarnings("Duplicates")
    public Statement persist(LearnerWalkable walkable, Deployment deployment) {
        final String UPSERT = "INSERT INTO learner.walkable_by_student_scope (" +
                "student_scope_urn" +
                ", deployment_id" +
                ", change_id" +
                ", walkable_id" +
                ", walkable_type) VALUES ( ?, ?, ?, ?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                walkable.getStudentScopeURN(),
                deployment.getId(),
                deployment.getChangeId(),
                walkable.getId(),
                walkable.getElementType().name()
        );
        stmt.setIdempotent(true);
        return stmt;
    }
}
