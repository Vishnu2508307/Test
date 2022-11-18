package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.AccountAction;

class AccountActionMutator extends SimpleTableMutator<AccountAction> {

    @Override
    public String getUpsertQuery(AccountAction mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.account_action ("
                + "  account_id"
                + ", action"
                + ", id"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountAction mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getAction().name(), mutation.getId());
    }

}
