package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class LearnerWalkableByStudentScopeMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerWalkableByStudentScopeMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findElement(UUID studentScopeURN, UUID deploymentId) {
        final String SELECT = "SELECT" +
                " walkable_id" +
                ", walkable_type FROM learner.walkable_by_student_scope" +
                " WHERE student_scope_urn = ?" +
                " AND deployment_id = ?" +
                " LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentScopeURN, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public CoursewareElement fromRow(Row row) {
        return new CoursewareElement()
                .setElementId(row.getUUID("walkable_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("walkable_type")));
    }
}
