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

public class DocumentItemByCoursewareElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentItemByCoursewareElementMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchAllItems(UUID elementId) {
        String SELECT = "SELECT" +
                " element_id," +
                " document_item_id," +
                " document_id," +
                " element_type" +
                " FROM competency.document_item_by_courseware_element" +
                " WHERE element_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(elementId);
        stmt.setIdempotent(true);
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
