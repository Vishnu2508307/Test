package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ResultNotificationByExportMutator extends SimpleTableMutator<ExportResultNotification> {

    @Override
    public Statement upsert(ExportResultNotification mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO export.result_notification_by_export (" +
                "  export_id" +
                ", notification_id" +
                ", element_id" +
                ", account_id" +
                ", element_type" +
                ", project_id" +
                ", workspace_id" +
                ", status" +
                ", completed_at" +
                ", root_element_id"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, ExportResultNotification mutation) {
        stmt.setUUID(0, mutation.getExportId());
        stmt.setUUID(1, mutation.getNotificationId());
        stmt.setUUID(2, mutation.getElementId());
        stmt.setUUID(3, mutation.getAccountId());
        stmt.setString(4, Enums.asString(mutation.getElementType()));
        stmt.setUUID(5, mutation.getProjectId());
        stmt.setUUID(6, mutation.getWorkspaceId());
        stmt.setString(7, Enums.asString(mutation.getStatus()));
        Mutators.bindNonNull(stmt, 8, mutation.getCompletedAt());
        stmt.setUUID(9, mutation.getRootElementId());
    }
}
