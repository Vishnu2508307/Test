package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CompetencyMetByStudentHistoryMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    private static final String BY_STUDENT = "SELECT " +
            "student_id, " +
            "document_id, " +
            "document_item_id, " +
            "met_id, " +
            "value, " +
            "confidence " +
            "FROM learner.competency_met_by_student_document_history " +
            "WHERE student_id = ?";

    @Inject
    public CompetencyMetByStudentHistoryMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement findAllHistoryByStudent(UUID studentId) {
        BoundStatement stmt = preparedStatementCache.asBoundStatement(BY_STUDENT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public Statement findAll(final UUID studentId, final UUID documentId, final UUID documentItemId) {
        String BY_ITEM = BY_STUDENT + " AND document_id = ?" +
                " AND document_item_id = ?";
        BoundStatement stmt = preparedStatementCache.asBoundStatement(BY_ITEM);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentId, documentId, documentItemId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public CompetencyMetByStudent fromRow(Row row) {
        return new CompetencyMetByStudent()
                .setStudentId(row.getUUID("student_id"))
                .setDocumentId(row.getUUID("document_id"))
                .setDocumentItemId(row.getUUID("document_item_id"))
                .setMetId(row.getUUID("met_id"))
                .setValue(row.getFloat("value"))
                .setConfidence(row.getFloat("confidence"));
    }
}
