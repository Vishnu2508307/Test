package com.smartsparrow.iam.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class AccountByHistoricalIdMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountByHistoricalIdMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchByHistoricalId(Long historicalId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  historical_id"
                + ", account_id"
                + ", iam_region"
                + " FROM iam_global.account_by_historical_id"
                + " WHERE historical_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(historicalId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
