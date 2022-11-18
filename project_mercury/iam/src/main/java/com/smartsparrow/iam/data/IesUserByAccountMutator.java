package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class IesUserByAccountMutator extends SimpleTableMutator<IESAccountTracking> {

    @Override
    public String getUpsertQuery(IESAccountTracking mutation) {
        return "INSERT INTO iam_global.ies_user_by_account (" +
                " account_id" +
                ", ies_user_id" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, IESAccountTracking mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getIesUserId());
    }
}
