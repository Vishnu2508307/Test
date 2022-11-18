package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ProjectMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ProjectMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findProject(final UUID id) {
        // @formatter:off
        final String QUERY = "SELECT" +
                " id" +
                ", workspace_id" +
                ", name" +
                ", config" +
                ", created_at" +
                " FROM workspace.project" +
                " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(id);
        return stmt;
    }

    public Project fromRow(final Row row) {
        return new Project()
                .setId(row.getUUID("id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setName(row.getString("name"))
                .setConfig(row.getString("config"))
                .setCreatedAt(row.getString("created_at"));
    }
}
