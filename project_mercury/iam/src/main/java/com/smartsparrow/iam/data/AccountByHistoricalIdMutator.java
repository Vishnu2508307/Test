package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.Account;

class AccountByHistoricalIdMutator extends SimpleTableMutator<Account> {

    @Override
    public String getUpsertQuery(Account mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.account_by_historical_id ("
                + "  historical_id"
                + ", account_id"
                + ", iam_region"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Account mutation) {
        stmt.bind(mutation.getHistoricalId(), mutation.getId(), mutation.getIamRegion().name());
    }

}
