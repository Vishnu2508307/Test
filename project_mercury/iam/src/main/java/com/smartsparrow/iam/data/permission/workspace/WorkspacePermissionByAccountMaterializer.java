package com.smartsparrow.iam.data.permission.workspace;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class WorkspacePermissionByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public WorkspacePermissionByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchPermission(UUID accountId, UUID workspaceId) {
        final String BY_ACCOUNT_WORKSPACE = "SELECT account_id, " +
                "workspace_id, " +
                "permission_level " +
                "FROM iam_global.workspace_permission_by_account " +
                "WHERE account_id = ? " +
                "AND workspace_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT_WORKSPACE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, workspaceId);
        stmt.setIdempotent(true);
        return stmt;
    }

}
