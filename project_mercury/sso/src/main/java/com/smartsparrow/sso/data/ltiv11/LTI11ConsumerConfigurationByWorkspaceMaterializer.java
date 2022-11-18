package com.smartsparrow.sso.data.ltiv11;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LTI11ConsumerConfigurationByWorkspaceMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    LTI11ConsumerConfigurationByWorkspaceMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByWorkspace(final UUID workspaceId) {
        final String SELECT = "SELECT" +
                " workspace_id" +
                ", id" +
                ", comment" +
                " FROM iam_global.lti11_consumer_configuration_by_workspace" +
                " WHERE workspace_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(workspaceId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LTIv11ConsumerConfiguration fromRow(final Row row) {
        return new LTIv11ConsumerConfiguration()
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setId(row.getUUID("id"))
                .setComment(row.getString("comment"));
    }
}