package com.smartsparrow.iam.data.permission.subscription;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

/**
 * This class allows to find the account permission level over a subscription entity. This table should be queried
 * when checking permissions
 */
public class SubscriptionPermissionByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    private static final String SELECT_BY_ACCOUNT = "SELECT " +
            "account_id, " +
            "subscription_id, " +
            "permission_level " +
            "FROM iam_global.subscription_permission_by_account " +
            "WHERE account_id = ?";

    @Inject
    public SubscriptionPermissionByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchPermissions(UUID accountId) {
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchPermission(UUID accountId, UUID subscriptionId) {
        final String AND_SUBSCRIPTION = " AND subscription_id = ?";
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_ACCOUNT + AND_SUBSCRIPTION);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, subscriptionId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convenience method to map a {@link Row} to a {@link AccountSubscriptionPermission} object
     * @param row the row to convert
     * @return a mapped {@link AccountSubscriptionPermission} object
     */
    public AccountSubscriptionPermission fromRow(Row row) {
        return new AccountSubscriptionPermission()
                .setAccountId(row.getUUID("account_id"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
