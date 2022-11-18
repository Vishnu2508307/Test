package com.smartsparrow.export.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

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

public class ExportSummaryByWorkspaceMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ExportSummaryByWorkspaceMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID workspaceId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  workspace_id" +
                ", export_id" +
                ", project_id" +
                ", element_id" +
                ", account_id" +
                ", element_type" +
                ", status" +
                ", completed_at" +
                ", ambrosia_url" +
                ", root_element_id" +
                ", export_type" +
                ", metadata" +
                " FROM export.summary_by_workspace" +
                " WHERE workspace_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(workspaceId);
        return stmt;
    }

    public ExportSummary fromRow(Row row) {
        return new ExportSummary()
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setId(row.getUUID("export_id"))
                .setProjectId(row.getUUID("project_id"))
                .setElementId(row.getUUID("element_id"))
                .setAccountId(row.getUUID("account_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setStatus(Enums.of(ExportStatus.class, row.getString("status")))
                .setCompletedAt(row.getUUID("completed_at"))
                .setAmbrosiaUrl(row.getString("ambrosia_url"))
                .setRootElementId(row.getUUID("root_element_id"))
                .setExportType(getNullableEnum(row, "export_type", ExportType.class))
                .setMetadata(row.getString("metadata"));
    }
}
