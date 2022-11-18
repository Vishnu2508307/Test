package com.smartsparrow.iam.data.permission.subscription;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.data.SubscriptionAccountCollaborator;

public class AccountSubscriptionCollaboratorMutator extends SimpleTableMutator<SubscriptionAccountCollaborator> {

    @Override
    public String getUpsertQuery(SubscriptionAccountCollaborator mutation) {
        return "INSERT INTO subscription.account_by_subscription (" +
                "subscription_id, " +
                "account_id, " +
                "permission_level) " +
                "VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(SubscriptionAccountCollaborator mutation) {
        return "DELETE FROM subscription.account_by_subscription " +
                "WHERE subscription_id = ? " +
                "AND account_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, SubscriptionAccountCollaborator mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getAccountId(), mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, SubscriptionAccountCollaborator mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getAccountId());
    }
}
