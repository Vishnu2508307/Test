package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.Region;

public class AccountAvatarMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountAvatarMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    /**
     * Find all the avatars for a given account.
     *
     * @param region the data region
     * @param accountId the account id
     * @return a statement that finds all the avatars for an account
     */
    @SuppressWarnings("Duplicates")
    public Statement fetchAllByAccount(Region region, UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", iam_region"
                + ", name"
                + ", mime_type"
                + ", meta"
                + ", data"
                + " FROM " + RegionKeyspace.map(region,"account_avatar")
                + " WHERE account_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Find all the avatars for a given account, no data payload.
     * @param region
     * @param accountId
     * @return
     */
    public Statement fetchAllInfoByAccount(Region region, UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", iam_region"
                + ", name"
                + ", mime_type"
                + ", meta"
                + " FROM " + RegionKeyspace.map(region,"account_avatar")
                + " WHERE account_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Find a specific avatar for an account.
     *
     * @param region the data region
     * @param accountId the account id
     * @param size the name/size of the image
     * @return a statment that finds a specific avatar for an account.
     */
    @SuppressWarnings("Duplicates")
    public Statement fetchBySizeAccount(Region region, UUID accountId, AccountAvatar.Size size) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", iam_region"
                + ", name"
                + ", mime_type"
                + ", meta"
                + ", data"
                + " FROM " + RegionKeyspace.map(region,"account_avatar")
                + " WHERE account_id=?"
                + "   AND name=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId, size.name());
        stmt.setIdempotent(true);
        return stmt;
    }
}
