package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ErrorNotificationByExportMutator extends SimpleTableMutator<ExportErrorNotification> {

    @Override
    public String getUpsertQuery(ExportErrorNotification mutation) {
        return "INSERT INTO export.error_notification_by_export (" +
                " export_id" +
                ", notification_id" +
                ", cause" +
                ", error_message" +
                ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ExportErrorNotification mutation) {
        stmt.setUUID(0, mutation.getExportId());
        stmt.setUUID(1, mutation.getNotificationId());
        stmt.setString(2, mutation.getCause());
        stmt.setString(3, mutation.getErrorMessage());
    }
}
