package com.smartsparrow.iam.data.permission.subscription;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

/**
 * This class allows to store an account permission level over a subscription entity. This table should be queried
 * when checking permissions
 */
public class SubscriptionPermissionByAccountMutator extends SimpleTableMutator<AccountSubscriptionPermission> {

    @Override
    public String getUpsertQuery(AccountSubscriptionPermission mutation) {
        return "INSERT INTO iam_global.subscription_permission_by_account (" +
                "account_id, " +
                "subscription_id, " +
                "permission_level) " +
                "VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountSubscriptionPermission mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getSubscriptionId(), mutation.getPermissionLevel().name());
    }
}
