package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;

import javax.inject.Inject;
import java.util.UUID;

class AlfrescoAssetSyncSummaryByCourseMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AlfrescoAssetSyncSummaryByCourseMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID courseId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                " course_id" +
                ", reference_id" +
                ", sync_type" +
                ", status" +
                ", completed_at" +
                "  FROM asset.alfresco_sync_summary_by_course" +
                "  WHERE course_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(courseId);
        return stmt;
    }

    public AlfrescoAssetSyncSummary fromRow(Row row) {
        return new AlfrescoAssetSyncSummary()
                .setCourseId(row.getUUID("course_id"))
                .setReferenceId(row.getUUID("reference_id"))
                .setSyncType(Enums.of(AlfrescoAssetSyncType.class, row.getString("sync_type")))
                .setStatus(Enums.of(AlfrescoAssetSyncStatus.class, row.getString("status")))
                .setCompletedAt(row.getUUID("completed_at"));
    }

}
