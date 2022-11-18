package com.smartsparrow.ingestion.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class SummaryByProjectMutator extends SimpleTableMutator<IngestionSummary> {

    @Override
    public String getUpsertQuery(IngestionSummary mutation) {
        return "INSERT INTO ingestion.summary_by_project (" +
                "  ingestion_id" +
                ", project_id" +
                ", workspace_id" +
                ", course_name" +
                ", config_fields" +
                ", creator_id" +
                ", ambrosia_url" +
                ", status" +
                ", ingestion_stats" +
                ", root_element_id" +
                ", activity_id" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, IngestionSummary mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getProjectId());
        stmt.setUUID(2, mutation.getWorkspaceId());
        stmt.setString(3, mutation.getCourseName());
        stmt.setString(4, mutation.getConfigFields());
        stmt.setUUID(5, mutation.getCreatorId());
        stmt.setString(6, mutation.getAmbrosiaUrl());
        stmt.setString(7, Enums.asString(mutation.getStatus()));
        stmt.setString(8, mutation.getIngestionStats());
        stmt.setUUID(9, mutation.getRootElementId());
        stmt.setUUID(10, mutation.getActivityId());
    }

    @Override
    public String getDeleteQuery(final IngestionSummary mutation) {
        return "DELETE FROM ingestion.summary_by_project" +
                " WHERE ingestion_id = ?" +
                " AND project_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final IngestionSummary mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getProjectId());
    }

    public Statement updateIngestionSummary(final IngestionSummary ingestionSummary) {
        final String UPDATE = "UPDATE ingestion.summary_by_project "
                + "SET status = ? "
                + ", ambrosia_url = ? "
                + ", ingestion_stats = ? "
                + ", root_element_id = ? "
                + ", activity_id = ? "
                + "WHERE ingestion_id = ? "
                + "AND project_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Enums.asString(ingestionSummary.getStatus()),
                  ingestionSummary.getAmbrosiaUrl(),
                  ingestionSummary.getIngestionStats(),
                  ingestionSummary.getRootElementId(),
                  ingestionSummary.getActivityId(),
                  ingestionSummary.getId(),
                  ingestionSummary.getProjectId());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement updateIngestionStatus(final IngestionSummary ingestionSummary) {
        final String UPDATE = "UPDATE ingestion.summary_by_project "
                + "SET status = ? "
                + "WHERE ingestion_id = ? "
                + "AND project_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Enums.asString(ingestionSummary.getStatus()),
                  ingestionSummary.getId(),
                  ingestionSummary.getProjectId());
        stmt.setIdempotent(true);
        return stmt;
    }
}
