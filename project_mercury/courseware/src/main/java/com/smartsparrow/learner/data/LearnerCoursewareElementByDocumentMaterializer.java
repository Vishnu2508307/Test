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
import com.smartsparrow.util.Enums;

public class LearnerCoursewareElementByDocumentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerCoursewareElementByDocumentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetch(UUID documentId, UUID deploymentId, UUID changeId) {
        String SELECT = "SELECT" +
                " document_id," +
                " deployment_id," +
                " change_id," +
                " element_id," +
                " document_item_id," +
                " element_type" +
                " FROM learner.courseware_element_by_document" +
                " WHERE document_id = ?" +
                " AND deployment_id = ?" +
                " AND change_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(documentId, deploymentId, changeId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public LearnerDocumentItemTag fromRow(Row row) {
        return new LearnerDocumentItemTag()
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setDocumentId(row.getUUID("document_id"))
                .setDocumentItemId(row.getUUID("document_item_id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"));
    }
}
