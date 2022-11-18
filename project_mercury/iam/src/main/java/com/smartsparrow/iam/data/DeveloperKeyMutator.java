package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.DeveloperKey;

public class DeveloperKeyMutator extends SimpleTableMutator<DeveloperKey> {
    @Override
    public String getUpsertQuery(DeveloperKey mutation) {
        return "INSERT INTO iam_global.developer_key (" +
                "key, " +
                "subscription_id, " +
                "account_id, " +
                "created_ts) " +
                "VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeveloperKey mutation) {
        stmt.bind(mutation.getKey(), mutation.getSubscriptionId(), mutation.getAccountId(), mutation.getCreatedTs());
    }
}
