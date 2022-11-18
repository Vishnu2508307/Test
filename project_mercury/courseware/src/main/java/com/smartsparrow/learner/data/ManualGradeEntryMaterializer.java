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
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.util.Enums;

public class ManualGradeEntryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ManualGradeEntryMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAll(UUID deploymentId, UUID studentId, UUID componentId, UUID attemptId) {
        final String SELECT = "SELECT" +
                " deployment_id" +
                ", student_id" +
                ", component_id" +
                ", attempt_id" +
                ", id" +
                ", max_score" +
                ", score" +
                ", change_id" +
                ", parent_id" +
                ", parent_type" +
                ", operator" +
                ", instructor_id" +
                " FROM learner.manual_grade_entry_by_student" +
                " WHERE deployment_id = ?" +
                " AND student_id = ?" +
                " AND component_id = ?" +
                " AND attempt_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, studentId, componentId, attemptId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public ManualGradeEntry fromRow(Row row) {
        return new ManualGradeEntry()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setStudentId(row.getUUID("student_id"))
                .setComponentId(row.getUUID("component_id"))
                .setAttemptId(row.getUUID("attempt_id"))
                .setId(row.getUUID("id"))
                .setMaxScore(row.getDouble("max_score"))
                .setScore(row.getDouble("score"))
                .setChangeId(row.getUUID("change_id"))
                .setParentId(row.getUUID("parent_id"))
                .setParentType(Enums.of(CoursewareElementType.class, row.getString("parent_type")))
                .setOperator(Enums.of(MutationOperator.class, row.getString("operator")))
                .setInstructorId(row.getUUID("instructor_id"));
    }
}
