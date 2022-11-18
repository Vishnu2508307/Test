package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

import javax.inject.Inject;
import java.util.UUID;

public class LearnerSearchableDocumentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerSearchableDocumentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatest(UUID deploymentId, UUID elementId, UUID searchableFieldId) {
        final String SELECT = "SELECT " +
                "deployment_id, " +
                "element_id, " +
                "searchable_field_id, " +
                "change_id, " +
                "product_id, " +
                "cohort_id, " +
                "element_type, " +
                "element_path, " +
                "element_path_type, " +
                "content_type, " +
                "summary, " +
                "body, " +
                "source, " +
                "preview, " +
                "tag " +
                "FROM learner.searchable_document " +
                "WHERE deployment_id = ? " +
                "AND element_id = ? " +
                "AND searchable_field_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, elementId, searchableFieldId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findElementIdsByDeployment(UUID deploymentId) {
        final String SELECT = "SELECT deployment_id, " +
                "element_id, " +
                "searchable_field_id, " +
                "change_id " +
                "FROM learner.searchable_document " +
                "WHERE deployment_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerSearchableDocument fromRow(Row row) {
        return new LearnerSearchableDocument()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setElementId(row.getUUID("element_id"))
                .setSearchableFieldId(row.getUUID("searchable_field_id"))
                .setChangeId(row.getUUID("change_id"))
                .setProductId(row.getString("product_id"))
                .setCohortId(row.getUUID("cohort_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setElementPath(row.getList("element_path", UUID.class))
                .setElementPathType(row.getList("element_path_type", String.class))
                .setContentType(row.getString("content_type"))
                .setSummary(row.getString("summary"))
                .setBody(row.getString("body"))
                .setSource(row.getString("source"))
                .setPreview(row.getString("preview"))
                .setTag(row.getString("tag"));
    }

    public LearnerSearchableDocumentIdentity identityFromRow(Row row) {
        return new LearnerSearchableDocumentIdentity()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setElementId(row.getUUID("element_id"))
                .setSearchableFieldId(row.getUUID("searchable_field_id"))
                .setChangeId(row.getUUID("change_id"));
    }

}
