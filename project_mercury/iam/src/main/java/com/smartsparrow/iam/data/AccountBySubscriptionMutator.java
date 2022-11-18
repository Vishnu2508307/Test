package com.smartsparrow.iam.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.Account;

class AccountBySubscriptionMutator extends SimpleTableMutator<Account> {

    @Override
    public String getUpsertQuery(Account mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.account_by_subscription ("
                + "  subscription_id"
                + ", account_id"
                + ") VALUES ( ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Account mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getId());
    }

    @Override
    public String getDeleteQuery(Account mutation) {
        // @formatter:off
        return "DELETE FROM iam_global.account_by_subscription"
                + "  WHERE subscription_id = ?"
                + "    AND account_id = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, Account mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getId());
    }

    public Statement addAccountSubscription(UUID accountId, UUID subscriptionId) {
        final String QUERY = "INSERT INTO iam_global.account_by_subscription ("
                + "  subscription_id"
                + ", account_id"
                + ") VALUES ( ?, ? )";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(subscriptionId, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

}
