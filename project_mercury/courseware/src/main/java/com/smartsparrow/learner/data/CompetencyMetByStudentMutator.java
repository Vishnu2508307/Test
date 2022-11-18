package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CompetencyMetByStudentMutator extends SimpleTableMutator<CompetencyMet> {

    @Override
    public String getUpsertQuery(CompetencyMet mutation) {
        return "INSERT INTO learner.competency_met_by_student_document ( " +
                "student_id, " +
                "document_id, " +
                "document_item_id, " +
                "met_id, " +
                "value, " +
                "confidence) " +
                "VALUES (?, ?, ?, ?, ?, ?);";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CompetencyMet mutation) {
        stmt.bind(mutation.getStudentId(),
                mutation.getDocumentId(),
                mutation.getDocumentItemId(),
                mutation.getId(),
                mutation.getValue(),
                mutation.getConfidence());
    }

    @Override
    public String getDeleteQuery(CompetencyMet mutation) {
        return "DELETE FROM learner.competency_met_by_student_document WHERE " + 
                "student_id = ? AND " +
                "document_id = ? AND " +
                "document_item_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, CompetencyMet mutation) {
        stmt.bind(mutation.getStudentId(), mutation.getDocumentId(), mutation.getDocumentItemId());
    }
}
