package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ProjectByWorkspaceMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ProjectByWorkspaceMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAll(final UUID workspaceId) {
        // @formatter:off
        final String QUERY = "SELECT" +
                " workspace_id" +
                ", project_id" +
                ", name" +
                ", created_at" +
                " FROM workspace.project_by_workspace" +
                " WHERE workspace_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(workspaceId);
        return stmt;
    }

    public ProjectByWorkspace fromRow(final Row row) {
        return new ProjectByWorkspace()
                .setProjectId(row.getUUID("project_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setName(row.getString("name"))
                .setCreatedAt(row.getString("created_at"));
    }
}
