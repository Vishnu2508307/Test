package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.DeveloperKey;

public class DeveloperKeyBySubscriptionMutator extends SimpleTableMutator<DeveloperKey> {
    @Override
    public String getUpsertQuery(DeveloperKey mutation) {
        return "INSERT INTO iam_global.developer_key_by_subscription (" +
                "subscription_id, " +
                "key, " +
                "account_id, " +
                "created_ts) " +
                "VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeveloperKey mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getKey(), mutation.getAccountId(), mutation.getCreatedTs());
    }
}
