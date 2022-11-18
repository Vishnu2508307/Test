package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.PermissionLevel;

/**
 * This mutator is used to store/delete the account subscription relationship dictated by permissions. Storing a row
 * will be equal to saying this account has access (any {@link PermissionLevel}) to the subscription.
 */
public class SubscriptionByAccountMutator extends SimpleTableMutator<SubscriptionAccount> {

    @Override
    public String getUpsertQuery(SubscriptionAccount mutation) {
        return "INSERT INTO subscription.subscription_by_account (" +
                "account_id, " +
                "subscription_id) " +
                "VALUES (?,?)";    }

    @Override
    public String getDeleteQuery(SubscriptionAccount mutation) {
        return "DELETE FROM subscription.subscription_by_account " +
                "WHERE account_id = ? " +
                "AND subscription_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, SubscriptionAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getSubscriptionId());
    }

    @Override
    public void bindDelete(BoundStatement stmt, SubscriptionAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getSubscriptionId());
    }
}
