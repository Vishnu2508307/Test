package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.Region;

public class AccountIdentityAttributesMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountIdentityAttributesMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement fetchByAccountId(Region region, UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", iam_region"
                + ", subscription_id"
                + ", given_name"
                + ", family_name"
                + ", honorific_prefix"
                + ", honorific_suffix"
                + ", email"
                + ", primary_email"
                + ", affiliation"
                + ", job_title"
                + " FROM " + RegionKeyspace.map(region,"account_identity_attribute")
                + " WHERE account_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
