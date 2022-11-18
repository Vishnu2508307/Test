package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

/**
 * This materializer is used to find all the subscriptions an account has access to.
 */
public class SubscriptionByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public SubscriptionByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchSubscriptions(UUID accountId) {
        final String SELECT_BY_ACCOUNT = "SELECT " +
                "subscription_id " +
                "FROM subscription.subscription_by_account " +
                "WHERE account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("subscription_id");
    }
}
