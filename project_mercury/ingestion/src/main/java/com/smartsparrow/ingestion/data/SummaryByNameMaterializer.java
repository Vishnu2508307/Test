package com.smartsparrow.ingestion.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class SummaryByNameMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public SummaryByNameMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findIngestionsByName(String courseName, UUID projectId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + " ingestion_id"
                + ", project_id"
                + ", course_name"
                + ", root_element_id"
                + " FROM ingestion.summary_by_name"
                + " WHERE course_name = ?"
                + " AND project_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(courseName, projectId);
        return stmt;
    }

    public IngestionSummary fromRow(Row row) {
        return new IngestionSummary()
                .setId(row.getUUID("ingestion_id"))
                .setProjectId(row.getUUID("project_id"))
                .setCourseName(row.getString("course_name"));
    }

    public Statement findIngestionsByNameRootElment(String courseName, UUID projectId, UUID rootElementId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + " ingestion_id"
                + ", project_id"
                + ", course_name"
                + ", root_element_id"
                + " FROM ingestion.summary_by_name"
                + " WHERE course_name = ?"
                + " AND project_id = ?"
                + " AND root_element_id = ? ALLOW FILTERING";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(courseName, projectId, rootElementId);
        return stmt;
    }

    public IngestionSummary fromRowWithElement(Row row) {
        return new IngestionSummary()
                .setId(row.getUUID("ingestion_id"))
                .setProjectId(row.getUUID("project_id"))
                .setCourseName(row.getString("course_name"))
                .setRootElementId(row.getUUID("root_element_id"));
    }
}
