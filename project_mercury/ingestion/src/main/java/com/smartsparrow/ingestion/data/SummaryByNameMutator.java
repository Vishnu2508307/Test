package com.smartsparrow.ingestion.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class SummaryByNameMutator extends SimpleTableMutator<IngestionSummary> {

    @Override
    public String getUpsertQuery(IngestionSummary mutation) {
        return "INSERT INTO ingestion.summary_by_name (" +
                "ingestion_id" +
                ", project_id" +
                ", course_name" +
                ", root_element_id" +
                ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, IngestionSummary mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getProjectId());
        stmt.setString(2, mutation.getCourseName());
        stmt.setUUID(3, mutation.getRootElementId());
    }

    @Override
    public String getDeleteQuery(final IngestionSummary mutation) {
        return "DELETE FROM ingestion.summary_by_name" +
                " WHERE ingestion_id = ?" +
                " AND course_name = ?" +
                " AND project_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final IngestionSummary mutation) {
        stmt.setUUID(0,mutation.getId());
        stmt.setString(1, mutation.getCourseName());
        stmt.setUUID(2, mutation.getProjectId());
    }
}
