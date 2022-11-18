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
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.Enums;

public class CompletedElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CompletedElementMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByAttempt(UUID deploymentId, UUID studentId, UUID elementId, UUID attemptId) {
        final String SELECT = "SELECT" +
                " deployment_id" +
                ", change_id" +
                ", student_id" +
                ", element_id" +
                ", element_attempt_id" +
                ", evaluation_id" +
                ", parent_element_id" +
                ", parent_element_attempt_id" +
                ", parent_element_type" +
                ", element_type" +
                " FROM learner.completed_element" +
                " WHERE deployment_id = ?" +
                " AND student_id = ?" +
                " AND element_id = ?" +
                " AND element_attempt_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.bind(deploymentId, studentId, elementId, attemptId);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public CompletedWalkable fromRow(Row row) {
        final UUID evaluationId = row.getUUID("evaluation_id");

        return new CompletedWalkable()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setStudentId(row.getUUID("student_id"))
                .setParentElementId(row.getUUID("parent_element_id"))
                .setParentElementAttemptId(row.getUUID("parent_element_attempt_id"))
                .setElementId(row.getUUID("element_id"))
                .setEvaluationId(evaluationId)
                .setElementAttemptId(row.getUUID("element_attempt_id"))
                .setParentElementType(Enums.of(CoursewareElementType.class, row.getString("parent_element_type")))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setEvaluatedAt(DateFormat.asRFC1123(evaluationId));    }
}
