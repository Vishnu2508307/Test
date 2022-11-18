package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;

class CredentialTemporaryByAccountMaterializer {

    @Inject
    private PreparedStatementCache stmtCache;

    public Statement findByAccountId(UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  authorization_code"
                + ", type"
                + ", account_id"
                + " FROM iam_global.credential_temporary_by_account"
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
