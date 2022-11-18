package com.smartsparrow.export.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.plugin.data.PublishMode;
import com.smartsparrow.util.Enums;

import javax.inject.Inject;
import java.util.UUID;

public class ExportSummaryByProjectMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ExportSummaryByProjectMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID projectId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  project_id" +
                ", export_id" +
                ", element_id" +
                ", account_id" +
                ", element_type" +
                ", workspace_id" +
                ", status" +
                ", completed_at"+
                ", ambrosia_url" +
                ", root_element_id" +
                ", export_type" +
                ", metadata" +
                " FROM export.summary_by_project" +
                " WHERE project_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(projectId);
        return stmt;
    }

    public ExportSummary fromRow(Row row) {
        return new ExportSummary()
                .setProjectId(row.getUUID("project_id"))
                .setId(row.getUUID("export_id"))
                .setElementId(row.getUUID("element_id"))
                .setAccountId(row.getUUID("account_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setStatus(Enums.of(ExportStatus.class, row.getString("status")))
                .setCompletedAt(row.getUUID("completed_at"))
                .setAmbrosiaUrl(row.getString("ambrosia_url"))
                .setRootElementId(row.getUUID("root_element_id"))
                .setExportType(getNullableEnum(row, "export_type", ExportType.class))
                .setMetadata(row.getString("metadata"));
    }
}
