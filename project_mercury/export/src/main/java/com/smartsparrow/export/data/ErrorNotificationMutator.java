package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ErrorNotificationMutator extends SimpleTableMutator<ExportErrorNotification> {

    @Override
    public Statement upsert(ExportErrorNotification mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO export.error_notification (" +
                "  notification_id" +
                ", error_message" +
                ", cause" +
                 ", export_id"
                + ") VALUES (?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, ExportErrorNotification mutation) {
        stmt.setUUID(0, mutation.getNotificationId());
        stmt.setString(1, mutation.getErrorMessage());
        stmt.setString(2, mutation.getCause());
        stmt.setUUID(3, mutation.getExportId());
    }
}
