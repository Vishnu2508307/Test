package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AccountByMyCloudUserMutator extends SimpleTableMutator<MyCloudAccountTracking> {

    @Override
    public String getUpsertQuery(MyCloudAccountTracking mutation) {
        return "INSERT INTO iam_global.account_by_mycloud_user (" +
                " mycloud_user_id" +
                ", account_id" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, MyCloudAccountTracking mutation) {
        stmt.bind(mutation.getMyCloudUserId(), mutation.getAccountId());
    }
}
