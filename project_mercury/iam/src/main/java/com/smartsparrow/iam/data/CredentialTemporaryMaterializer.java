package com.smartsparrow.iam.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;

class CredentialTemporaryMaterializer {

    @Inject
    private PreparedStatementCache stmtCache;

    public Statement fetchByAuthorizationCode(String authorizationCode) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  authorization_code"
                + ", type"
                + ", account_id"
                + " FROM iam_global.credential_temporary"
                + " WHERE authorization_code = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(authorizationCode);
        stmt.setIdempotent(true);
        return stmt;
    }
}
