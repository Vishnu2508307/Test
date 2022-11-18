package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WalkableByStudentScopeMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID studentScopeURN, CoursewareElement walkable) {
        // @formatter:off
        String QUERY = "INSERT INTO courseware.walkable_by_student_scope ("
                + "  student_scope_urn"
                + ", walkable_id"
                + ", walkable_type"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentScopeURN, walkable.getElementId(), walkable.getElementType().name());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement delete(UUID studentScopeURN, UUID walkableId) {
        // @formatter:off
        String QUERY = "DELETE FROM courseware.walkable_by_student_scope"
                + " WHERE student_scope_urn = ?"
                + " AND walkable_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentScopeURN, walkableId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
