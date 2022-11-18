package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;
import java.util.UUID;

public class ErrorNotificationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ErrorNotificationMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID notificationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  notification_id" +
                ", error_message" +
                ", cause" +
                ", export_id" +
                " FROM export.error_notification" +
                " WHERE notification_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(notificationId);
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
