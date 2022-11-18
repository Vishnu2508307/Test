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

class IngestionSummaryMaterializer implements TableMaterializer {
    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public IngestionSummaryMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findById(UUID id) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
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
                + " FROM ingestion.summary"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(id);
        return stmt;
    }

    public IngestionSummary fromRow(Row row) {
        return new IngestionSummary()
                .setId(row.getUUID("id"))
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

    public Statement findByRootElementId(UUID rootElementId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", project_id"
                + ", root_element_id"
                + ", workspace_id"
                + ", course_name"
                + ", config_fields"
                + ", creator_id"
                + ", ambrosia_url"
                + ", status"
                + ", ingestion_stats"
                + ", activity_id"
                + " FROM ingestion.summary"
                + " WHERE root_element_id = ? ALLOW FILTERING";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(rootElementId);
        return stmt;
    }

}
