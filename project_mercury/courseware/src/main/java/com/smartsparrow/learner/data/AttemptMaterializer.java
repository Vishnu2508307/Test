package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.util.Enums;

class AttemptMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AttemptMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(final UUID attemptId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", parent_id"
                + ", deployment_id"
                + ", courseware_element_id"
                + ", courseware_element_type"
                + ", student_id"
                + ", value"
                + " FROM learner.attempt"
                + " WHERE id = ?";
        // @formatter:on

        @SuppressWarnings("Duplicates")
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(attemptId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public Attempt fromRow(final Row row) {
        return new Attempt() //
                .setId(row.getUUID("id"))
                .setParentId(row.getUUID("parent_id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setCoursewareElementId(row.getUUID("courseware_element_id"))
                .setCoursewareElementType(Enums.of(CoursewareElementType.class, //
                                                   row.getString("courseware_element_type")))
                .setStudentId(row.getUUID("student_id"))
                .setValue(row.getInt("value"));
    }
}
