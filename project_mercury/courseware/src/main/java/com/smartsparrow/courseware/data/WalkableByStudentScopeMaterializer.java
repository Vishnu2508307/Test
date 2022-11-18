package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class WalkableByStudentScopeMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public WalkableByStudentScopeMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findWalkable(UUID studentScopeURN) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " walkable_id"
                + ", walkable_type"
                + " FROM courseware.walkable_by_student_scope"
                + " WHERE student_scope_urn = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(studentScopeURN);
        return stmt;
    }

    public CoursewareElement fromRow(Row row) {
        return new CoursewareElement()
                .setElementId(row.getUUID("walkable_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("walkable_type")));
    }
}
