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

public class ResultNotificationByExportMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ResultNotificationByExportMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID exportId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  export_id" +
                ", notification_id" +
                ", element_id" +
                ", account_id" +
                ", element_type" +
                ", project_id" +
                ", workspace_id" +
                ", status" +
                ", completed_at" +
                ", root_element_id" +
                " FROM export.result_notification_by_export" +
                " WHERE export_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(exportId);
        return stmt;
    }

    public ExportResultNotification fromRow(Row row) {
        return new ExportResultNotification()
                .setExportId(row.getUUID("export_id"))
                .setNotificationId(row.getUUID("notification_id"))
                .setElementId(row.getUUID("element_id"))
                .setAccountId(row.getUUID("account_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setProjectId(row.getUUID("project_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setStatus(Enums.of(ExportStatus.class, row.getString("status")))
                .setCompletedAt(row.getUUID("completed_at"))
                .setRootElementId(row.getUUID("root_element_id"));
    }
}
