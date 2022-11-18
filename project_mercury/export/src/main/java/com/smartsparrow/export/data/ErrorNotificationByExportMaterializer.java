package com.smartsparrow.export.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ErrorNotificationByExportMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ErrorNotificationByExportMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findErrors(final UUID exportId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  export_id" +
                ", notification_id" +
                ", cause" +
                ", error_message" +
                " FROM export.error_notification_by_export" +
                " WHERE export_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(exportId);
        return stmt;
    }

    public ExportErrorNotification fromRow(Row row) {
        return new ExportErrorNotification()
                .setNotificationId(row.getUUID("notification_id"))
                .setErrorMessage(row.getString("error_message"))
                .setCause(row.getString("cause"))
                .setExportId(row.getUUID("export_id"));
    }
}
