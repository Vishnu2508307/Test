package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AccountByIesUserMutator extends SimpleTableMutator<IESAccountTracking> {

    @Override
    public String getUpsertQuery(IESAccountTracking mutation) {
        return "INSERT INTO iam_global.account_by_ies_user (" +
                " ies_user_id" +
                ", account_id" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, IESAccountTracking mutation) {
        stmt.bind(mutation.getIesUserId(), mutation.getAccountId());
    }
}
