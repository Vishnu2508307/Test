package com.smartsparrow.sso.data.ltiv11;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LTI11ConsumerConfigurationMaterializer  implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    LTI11ConsumerConfigurationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID id) {
        final String SELECT = "SELECT FROM iam_global.lti11_consumer_configuration" +
                " id" +
                ", workspace_id" +
                ", comment" +
                " WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
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
