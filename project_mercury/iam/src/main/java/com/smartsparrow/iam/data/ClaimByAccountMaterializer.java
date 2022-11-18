package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.Claim;
import com.smartsparrow.iam.service.Region;

class ClaimByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    ClaimByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(Region region, UUID accountId, UUID subscriptionId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", subscription_id"
                + ", name"
                + ", value"
                + " FROM " + RegionKeyspace.map(region, "claim_by_account")
                + " WHERE account_id = ?"
                + "   AND subscription_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, subscriptionId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Claim fromRow(Row row) {
        return new Claim() //
                .setAccountId(row.getUUID("account_id"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setName(row.getString("name"))
                .setValue(row.getString("value"));
    }
}
