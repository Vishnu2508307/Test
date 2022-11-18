package com.smartsparrow.iam.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.Region;

class AccountAvatarMutator extends SimpleTableMutator<AccountAvatar> {

    @Override
    public String getUpsertQuery(AccountAvatar mutation) {
        // @formatter:off
        return "INSERT INTO " + RegionKeyspace.map(mutation.getIamRegion(), "account_avatar") + " ("
                + "  account_id"
                + ", iam_region"
                + ", name"
                + ", mime_type"
                + ", meta"
                + ", data"
                + ") VALUES ( ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountAvatar mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getIamRegion().name(), mutation.getName().name(),
                  mutation.getMimeType(), mutation.getMeta(), mutation.getData());
    }

    @Override
    public ConsistencyLevel upsertConsistencyLevel() {
        return isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM;
    }

    @Override
    public String getDeleteQuery(AccountAvatar mutation) {
        // @formatter:off
        return "DELETE FROM " + RegionKeyspace.map(mutation.getIamRegion(), "account_avatar")
                + "  WHERE account_id = ?"
                + "    AND name = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountAvatar mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getName().name());
    }

    @Override
    public ConsistencyLevel deleteConsistencyLevel() {
        return isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM;
    }

    /**
     * Delete all the account avatars for the specified account in the specified region
     *
     * @param region the region
     * @param accountId the account
     * @return a statment to do so
     */
    public Statement deleteAll(Region region, UUID accountId) {
        final String QUERY = "DELETE FROM " + RegionKeyspace.map(region, "account_avatar") //
                + " WHERE account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
