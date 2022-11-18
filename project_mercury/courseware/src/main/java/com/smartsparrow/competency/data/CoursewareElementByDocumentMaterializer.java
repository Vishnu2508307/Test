package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class CoursewareElementByDocumentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CoursewareElementByDocumentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement findElements(UUID documentId) {
        String SELECT = "SELECT" +
                " document_id," +
                " element_id," +
                " document_item_id," +
                " element_type" +
                " FROM competency.courseware_element_by_document" +
                " WHERE document_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setIdempotent(true);
        stmt.bind(documentId);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        return stmt;
    }

    public DocumentItemTag fromRow(Row row) {
        return new DocumentItemTag()
                .setDocumentId(row.getUUID("document_id"))
                .setDocumentItemId(row.getUUID("document_item_id"))
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")));
    }
}
