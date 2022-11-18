package com.smartsparrow.ingestion.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class IngestionEventMutator extends SimpleTableMutator<IngestionEvent> {

    @Override
    public String getUpsertQuery(IngestionEvent mutation) {
        return "INSERT INTO ingestion.event (" +
                "  id" +
                ", ingestion_id" +
                ", project_id" +
                ", event_type" +
                ", code" +
                ", message" +
                ", error" +
                ", action" +
                ", location" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, IngestionEvent mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getIngestionId());
        stmt.setUUID(2, mutation.getProjectId());
        stmt.setString(3, Enums.asString(mutation.getEventType()));
        stmt.setString(4, mutation.getCode());
        stmt.setString(5, mutation.getMessage());
        stmt.setString(6, mutation.getError());
        stmt.setString(7, mutation.getAction());
        stmt.setString(8, mutation.getLocation());
    }

    @Override
    public String getDeleteQuery(final IngestionEvent mutation) {
        return "DELETE FROM ingestion.event" +
                " WHERE ingestion_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final IngestionEvent mutation) {
        stmt.bind(mutation.getIngestionId());
    }
}
