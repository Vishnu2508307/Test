package com.smartsparrow.iam.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class AccountByHashMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountByHashMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchByHash(String hash) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  hash"
                + ", account_id"
                + ", iam_region"
                + " FROM iam_global.account_by_hash"
                + " WHERE hash=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(hash);
        stmt.setIdempotent(true);
        return stmt;
    }
}
