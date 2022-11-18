package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DeletedWorkspaceByIdMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    DeletedWorkspaceByIdMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchDeletedWorkspacesById(final UUID workspaceId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  workspace_id"
                + " , name"
                + ", account_id"
                + ", deleted_at"
                + " FROM workspace.deleted_workspace_by_id"
                + " WHERE workspace_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(workspaceId);
        return stmt;
    }

    public DeletedWorkspace fromRow(Row row) {
        return new DeletedWorkspace()
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setName(row.getString("name"))
                .setAccountId(row.getUUID("account_id"))
                .setDeletedAt(row.getString("deleted_at"));
    }
}
