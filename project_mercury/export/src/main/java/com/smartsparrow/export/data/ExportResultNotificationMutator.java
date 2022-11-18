package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ExportResultNotificationMutator extends SimpleTableMutator<ExportResultNotification> {

    @Override
    public Statement upsert(ExportResultNotification mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO export.result_notification (" +
                "  element_id" +
                ", account_id" +
                ", element_type" +
                ", project_id" +
                ", workspace_id" +
                ", status" +
                ", notification_id" +
                ", export_id" +
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
        stmt.setUUID(0, mutation.getElementId());
        stmt.setUUID(1, mutation.getAccountId());
        stmt.setString(2, Enums.asString(mutation.getElementType()));
        stmt.setUUID(3, mutation.getProjectId());
        stmt.setUUID(4, mutation.getWorkspaceId());
        stmt.setString(5, Enums.asString(mutation.getStatus()));
        stmt.setUUID(6, mutation.getNotificationId());
        stmt.setUUID( 7, mutation.getExportId());
        Mutators.bindNonNull(stmt, 8, mutation.getCompletedAt());
        stmt.setUUID(9, mutation.getRootElementId());
    }
}
