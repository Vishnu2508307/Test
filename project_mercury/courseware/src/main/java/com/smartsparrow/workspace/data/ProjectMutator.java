package com.smartsparrow.workspace.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ProjectMutator extends SimpleTableMutator<Project> {

    @Override
    public String getUpsertQuery(final Project mutation) {
        return "INSERT INTO workspace.project (" +
                " id" +
                ", workspace_id" +
                ", name" +
                ", config" +
                ", created_at" +
                ") VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final Project mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getWorkspaceId(),
                mutation.getName(),
                mutation.getConfig(),
                mutation.getCreatedAt()
        );
    }

    @Override
    public String getDeleteQuery(final Project mutation) {
        return "DELETE FROM workspace.project" +
                " WHERE id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final Project mutation) {
        stmt.bind(mutation.getId());
    }

    public Statement updateConfig(final UUID id, final String config) {
        final String UPDATE_CONFIG = "UPDATE workspace.project" +
                " SET config = ?" +
                " WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE_CONFIG);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(config, id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement updateName(final UUID id, final String name) {
        final String UPDATE_NAME = "UPDATE workspace.project" +
                " SET name = ?" +
                " WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE_NAME);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(name, id);
        stmt.setIdempotent(true);
        return stmt;
    }
}
