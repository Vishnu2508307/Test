package com.smartsparrow.ingestion.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class SummaryByProjectMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public SummaryByProjectMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findIngestions(UUID projectId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  ingestion_id"
                + ", project_id"
                + ", workspace_id"
                + ", course_name"
                + ", config_fields"
                + ", creator_id"
                + ", ambrosia_url"
                + ", status"
                + ", ingestion_stats"
                + ", root_element_id"
                + ", activity_id"
                + " FROM ingestion.summary_by_project"
                + " WHERE project_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(projectId);
        return stmt;
    }

    public IngestionSummary fromRow(Row row) {
        return new IngestionSummary()
                .setId(row.getUUID("ingestion_id"))
                .setProjectId(row.getUUID("project_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setCourseName(row.getString("course_name"))
                .setConfigFields(row.getString("config_fields"))
                .setCreatorId(row.getUUID("creator_id"))
                .setAmbrosiaUrl(row.getString("ambrosia_url"))
                .setStatus(Enums.of(IngestionStatus.class, row.getString("status")))
                .setIngestionStats(row.getString("ingestion_stats"))
                .setRootElementId(row.getUUID("root_element_id"))
                .setActivityId(row.getUUID("activity_id"));
    }
}
