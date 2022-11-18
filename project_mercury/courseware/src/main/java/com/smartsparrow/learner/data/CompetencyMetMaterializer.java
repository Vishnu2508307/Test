package com.smartsparrow.learner.data;


import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CompetencyMetMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public CompetencyMetMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findById(UUID id) {
        final String BY_ID = "SELECT id, " +
                "student_id, " +
                "deployment_id, " +
                "change_id, " +
                "element_id, " +
                "element_type, " +
                "evaluation_id, " +
                "document_id, " +
                "document_version_id, " +
                "item_id, " +
                "attempt_id, " +
                "value, " +
                "confidence " +
                "FROM learner.competency_met " +
                "WHERE id = ?";

        BoundStatement stmt = preparedStatementCache.asBoundStatement(BY_ID);
        stmt.bind(id);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        return stmt;
    }

    public CompetencyMet fromRow(Row row) {
        return new CompetencyMet()
                .setId(row.getUUID("id"))
                .setStudentId(row.getUUID("student_id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setCoursewareElementId(row.getUUID("element_id"))
                .setEvaluationId(row.getUUID("evaluation_id"))
                .setDocumentId(row.getUUID("document_id"))
                .setDocumentVersionId(row.getUUID("document_version_id"))
                .setDocumentItemId(row.getUUID("item_id"))
                .setAttemptId(row.getUUID("attempt_id"))
                .setValue(row.getFloat("value"))
                .setConfidence(row.getFloat("confidence"))
                .setCoursewareElementType(getNullableEnum(row, "element_type", CoursewareElementType.class));
    }
}
