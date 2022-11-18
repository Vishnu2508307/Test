package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class AccountBySubscriptionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountBySubscriptionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    /**
     * Find all the accounts on a particular subscription.
     *
     * @param subscriptionId the subscription id
     * @return
     */
    @SuppressWarnings("Duplicates")
    public Statement fetchAllBySubscription(UUID subscriptionId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  subscription_id"
                + ", account_id"
                + " FROM iam_global.account_by_subscription"
                + " WHERE subscription_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(subscriptionId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Find an account on a particular subscription.
     *
     * @param subscriptionId the subscription id
     * @param accountId the account id
     * @return
     */
    @SuppressWarnings("Duplicates")
    public Statement fetchAccountOnSubscription(UUID subscriptionId, UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  subscription_id"
                + ", account_id"
                + " FROM iam_global.account_by_subscription"
                + " WHERE subscription_id=?"
                + "   AND accountId=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(subscriptionId, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
