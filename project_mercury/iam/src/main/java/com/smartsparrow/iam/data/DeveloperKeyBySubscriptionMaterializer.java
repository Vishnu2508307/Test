package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DeveloperKeyBySubscriptionMaterializer implements TableMaterializer {

    private PreparedStatementCache stmtCache;

    @Inject
    public DeveloperKeyBySubscriptionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchBySubscription(UUID subscriptionId) {
        final String QUERY = "SELECT " +
                "subscription_id, " +
                "key, " +
                "account_id, " +
                "created_ts " +
                "FROM iam_global.developer_key_by_subscription " +
                "WHERE subscription_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(subscriptionId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
