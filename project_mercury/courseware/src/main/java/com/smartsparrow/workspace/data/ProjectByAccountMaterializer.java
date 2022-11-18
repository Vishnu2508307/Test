package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ProjectByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ProjectByAccountMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchProjectsForAccount(final UUID accountId, final UUID workspaceId) {
        // @formatter:off
        final String QUERY = "SELECT" +
                " project_id" +
                " FROM workspace.project_by_account_workspace" +
                " WHERE account_id = ?" +
                " AND workspace_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(accountId, workspaceId);
        return stmt;
    }

    public UUID fromRow(final Row row) {
        return row.getUUID("project_id");
    }
}
