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

class IngestionEventMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public IngestionEventMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findById(UUID id) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", ingestion_id"
                + ", project_id"
                + ", event_type"
                + ", code"
                + ", message"
                + ", error"
                + ", action"
                + ", location"
                + " FROM ingestion.event"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(id);
        return stmt;
    }

    public IngestionEvent fromRow(Row row) {
        return new IngestionEvent()
                .setId(row.getUUID("id"))
                .setIngestionId(row.getUUID("ingestion_id"))
                .setProjectId(row.getUUID("project_id"))
                .setEventType(Enums.of(IngestionEventType.class, row.getString("event_type")))
                .setCode(row.getString("code"))
                .setMessage(row.getString("message"))
                .setError(row.getString("error"))
                .setAction(row.getString("action"))
                .setLocation(row.getString("location"));
    }
}
