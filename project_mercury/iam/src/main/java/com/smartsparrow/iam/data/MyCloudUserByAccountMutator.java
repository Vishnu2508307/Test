package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class MyCloudUserByAccountMutator extends SimpleTableMutator<MyCloudAccountTracking> {

    @Override
    public String getUpsertQuery(MyCloudAccountTracking mutation) {
        return "INSERT INTO iam_global.mycloud_user_by_account (" +
                " account_id" +
                ", mycloud_user_id" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, MyCloudAccountTracking mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getMyCloudUserId());
    }
}
