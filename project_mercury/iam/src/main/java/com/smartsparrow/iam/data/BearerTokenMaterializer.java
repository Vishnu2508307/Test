package com.smartsparrow.iam.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;

public class BearerTokenMaterializer {

    @Inject
    private PreparedStatementCache stmtCache;

    public Statement fetchByToken(String token) {
        // @formatter:off
        final String QUERY = "SELECT "
                            + "  key"
                            + ", account_id"
                            + " FROM iam_global.bearer_token"
                            + " WHERE key = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(token);
        stmt.setIdempotent(true);
        return stmt;
    }
}
