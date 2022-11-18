package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class SubscriptionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    // @formatter:off
    private static final String FETCH_SUBSCRIPTION = "SELECT "
            + "  id"
            + ", name"
            + ", iam_region"
            + " FROM iam_global.subscription"
            + " WHERE id=?";
    // @formatter:on

    @Inject
    SubscriptionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchSubscription(UUID id) {
        BoundStatement stmt = stmtCache.asBoundStatement(FETCH_SUBSCRIPTION);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }
}
