package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class WorkspaceByProjectMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public WorkspaceByProjectMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findWorkspaceId(final UUID projectId) {
        // @formatter:off
        final String QUERY = "SELECT" +
                " project_id" +
                ", workspace_id" +
                " FROM workspace.workspace_by_project" +
                " WHERE project_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(projectId);
        return stmt;
    }

    public WorkspaceProject fromRow(final Row row) {
        return new WorkspaceProject()
                .setProjectId(row.getUUID("project_id"))
                .setWorkspaceId(row.getUUID("workspace_id"));
    }
}
