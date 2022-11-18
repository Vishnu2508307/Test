package com.smartsparrow.iam.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.AccountLogEntry;
import com.smartsparrow.iam.service.Region;

class AccountLogEntryMutator extends SimpleTableMutator<AccountLogEntry> {

    @Override
    public String getUpsertQuery(AccountLogEntry mutation) {
        // @formatter:off
        return "INSERT INTO " + RegionKeyspace.map(mutation.getIamRegion(), "account_log") + " ("
                + "  account_id"
                + ", iam_region"
                + ", id"
                + ", action"
                + ", on_behalf_of"
                + ", message"
                + ") VALUES ( ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountLogEntry mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getIamRegion().name(), mutation.getId(),
                  mutation.getAction().name(), mutation.getOnBehalfOf(), mutation.getMessage());
    }

    @Override
    public ConsistencyLevel upsertConsistencyLevel() {
        return isForceLocalCL() ? ConsistencyLevel.LOCAL_ONE : ConsistencyLevel.ONE;
    }

    /**
     * Delete all the log entries for the specified account in the provided region.
     *
     * @param region the region
     * @param accountId the account id.
     * @return a statement to do so.
     */
    public Statement deleteAll(Region region, UUID accountId) {
        final String QUERY = "DELETE FROM " + RegionKeyspace.map(region, "account_log") //
                + " WHERE account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
