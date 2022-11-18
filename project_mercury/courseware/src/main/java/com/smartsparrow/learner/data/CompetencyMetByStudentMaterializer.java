package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CompetencyMetByStudentMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    private static final String BY_STUDENT = "SELECT " +
            "student_id, " +
            "document_id, " +
            "document_item_id, " +
            "met_id, " +
            "value, " +
            "confidence " +
            "FROM learner.competency_met_by_student_document " +
            "WHERE student_id = ?";

    @Inject
    public CompetencyMetByStudentMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement findByStudent(UUID studentId) {
        BoundStatement stmt = preparedStatementCache.asBoundStatement(BY_STUDENT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findByDocument(UUID studentId, UUID documentId) {
        final String BY_DOCUMENT = BY_STUDENT + " AND document_id = ?";

        BoundStatement stmt = preparedStatementCache.asBoundStatement(BY_DOCUMENT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentId, documentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement  findByDocumentItem(UUID studentId, UUID documentId, UUID itemId) {
        final String BY_DOCUMENT = BY_STUDENT + " AND document_id = ? AND document_item_id = ?";

        BoundStatement stmt = preparedStatementCache.asBoundStatement(BY_DOCUMENT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentId, documentId, itemId);
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
