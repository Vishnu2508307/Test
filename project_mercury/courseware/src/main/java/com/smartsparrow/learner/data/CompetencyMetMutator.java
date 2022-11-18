package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CompetencyMetMutator extends SimpleTableMutator<CompetencyMet> {

    @Override
    public String getUpsertQuery(CompetencyMet mutation) {
        return "INSERT INTO learner.competency_met (" +
                "id, " +
                "student_id, " +
                "document_id, " +
                "document_version_id, " +
                "item_id, " +
                "value, " +
                "confidence, " +
                "deployment_id, " +
                "change_id, " +
                "element_id, " +
                "evaluation_id, " +
                "attempt_id, " +
                "element_type) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CompetencyMet mutation) {
        stmt
            .setUUID(0, mutation.getId())
            .setUUID(1, mutation.getStudentId())
            .setUUID(2, mutation.getDocumentId())
            .setUUID(3, mutation.getDocumentVersionId())
            .setUUID(4, mutation.getDocumentItemId())
            .setFloat(5, mutation.getValue())
            .setFloat(6, mutation.getConfidence());

        // Optional fields leave unset to avoid tombstones
        optionalBind(stmt, 7, mutation.getDeploymentId(), UUID.class);
        optionalBind(stmt, 8, mutation.getChangeId(), UUID.class);
        optionalBind(stmt, 9, mutation.getCoursewareElementId(), UUID.class);
        optionalBind(stmt, 10, mutation.getEvaluationId(), UUID.class);
        optionalBind(stmt, 11, mutation.getAttemptId(), UUID.class);

        if(mutation.getCoursewareElementType() != null) {
            stmt.setString(12, mutation.getCoursewareElementType().name());
        }
    }

    @Override
    public String getDeleteQuery(CompetencyMet mutation) {
        return "DELETE FROM learner.competency_met WHERE id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, CompetencyMet mutation) {
        stmt.bind(mutation.getId());
    }
}
