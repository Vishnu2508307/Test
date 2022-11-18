package com.smartsparrow.export.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

import javax.inject.Inject;
import java.util.UUID;

public class ExportResultNotificationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ExportResultNotificationMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID notificationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  element_id" +
                ", account_id" +
                ", element_type" +
                ", project_id" +
                ", workspace_id" +
                ", status" +
                ", notification_id" +
                ", export_id" +
                ", completed_at" +
                ", root_element_id" +
                " FROM export.result_notification" +
                " WHERE notification_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(notificationId);
        return stmt;
    }

    public ExportResultNotification fromRow(Row row) {
        return new ExportResultNotification()
                .setElementId(row.getUUID("element_id"))
                .setAccountId(row.getUUID("account_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setProjectId(row.getUUID("project_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setStatus(Enums.of(ExportStatus.class, row.getString("status")))
                .setNotificationId(row.getUUID("notification_id"))
                .setExportId(row.getUUID("export_id"))
                .setCompletedAt(row.getUUID("completed_at"))
                .setRootElementId(row.getUUID("root_element_id"));
    }
}
