package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

public class WorkspaceAccountCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    WorkspaceAccountCollaboratorMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchAccountsForWorkspace(final UUID workspaceId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  workspace_id"
                + ", account_id"
                + ", permission_level"
                + " FROM workspace.account_by_workspace"
                + " WHERE workspace_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(workspaceId);
        return stmt;
    }

    public WorkspaceAccountCollaborator fromRow(Row row) {
        return new WorkspaceAccountCollaborator()
                .setAccountId(row.getUUID("account_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
