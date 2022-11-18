package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.Region;

public class AccountLogEventMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountLogEventMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    /**
     * Find all log entries for a given account
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
                + ", id"
                + ", action"
                + ", on_behalf_of"
                + ", message"
                + " FROM " + RegionKeyspace.map(region,"account_log")
                + " WHERE account_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
