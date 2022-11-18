package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.AccountHash;

class AccountByHashMutator extends SimpleTableMutator<AccountHash> {

    @Override
    public String getUpsertQuery(AccountHash mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.account_by_hash ("
                + "  hash"
                + ", account_id"
                + ", iam_region"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountHash mutation) {
        stmt.bind(mutation.getHash(), mutation.getAccountId(), mutation.getIamRegion().name());
    }

    @Override
    public String getDeleteQuery(AccountHash mutation) {
        // @formatter:off
        return "DELETE FROM iam_global.account_by_hash"
                + "  WHERE hash = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountHash mutation) {
        stmt.bind(mutation.getHash());
    }

}
