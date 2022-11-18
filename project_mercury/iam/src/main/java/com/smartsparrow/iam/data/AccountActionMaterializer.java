package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class AccountActionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountActionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchByAccount(UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", action"
                + ", id"
                + " FROM iam_global.account_action"
                + " WHERE account_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

}
