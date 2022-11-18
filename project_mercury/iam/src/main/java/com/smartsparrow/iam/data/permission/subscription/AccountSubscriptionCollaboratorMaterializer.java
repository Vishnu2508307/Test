package com.smartsparrow.iam.data.permission.subscription;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.data.SubscriptionAccountCollaborator;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

public class AccountSubscriptionCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AccountSubscriptionCollaboratorMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchCollaborators(UUID subscriptionId) {

        final String SELECT_BY_SUBSCRIPTION = "SELECT " +
                "subscription_id, " +
                "account_id, " +
                "permission_level " +
                "FROM subscription.account_by_subscription " +
                "WHERE subscription_id = ?";
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_SUBSCRIPTION);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(subscriptionId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public SubscriptionAccountCollaborator fromRow(Row row) {
        return new SubscriptionAccountCollaborator()
                .setAccountId(row.getUUID("account_id"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }

}
