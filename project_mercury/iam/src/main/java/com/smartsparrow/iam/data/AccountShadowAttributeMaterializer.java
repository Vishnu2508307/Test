package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.iam.service.Region;

public class AccountShadowAttributeMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountShadowAttributeMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    /**
     * Find all the attributes for a specific account.
     *
     * @param region the data region to query
     * @param accountId the account id
     * @return a {@code Statement} to find all the attributes for a specific account.
     */
    @SuppressWarnings("Duplicates")
    public Statement fetchAllForAccount(Region region, UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", iam_region"
                + ", name"
                + ", value"
                + ", source"
                + " FROM " + RegionKeyspace.map(region,"account_shadow_attribute")
                + " WHERE account_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Find all the values for a name on a given account.
     *
     * @param region the data region to query
     * @param accountId the account id
     * @param name the name of the attribute
     * @return a {@code Statement} to find all the values of a name on a given account.
     */
    @SuppressWarnings("Duplicates")
    public Statement fetchAttributeForAccount(Region region, UUID accountId, AccountShadowAttributeName name) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", iam_region"
                + ", name"
                + ", value"
                + ", source"
                + " FROM " + RegionKeyspace.map(region,"account_shadow_attribute")
                + " WHERE account_id=?"
                + "   AND name=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId, name.name());
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Find a specific value on a given account and name.
     *
     * @param region the data region to query
     * @param accountId the account id
     * @param name the name of the attribute
     * @param value the value of the name attribute
     * @return a {@code Statement} to find a specific value on a given account and name.
     */
    @SuppressWarnings("Duplicates")
    public Statement fetchAttributeForAccount(Region region,
            UUID accountId,
            AccountShadowAttributeName name,
            String value) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", iam_region"
                + ", name"
                + ", value"
                + ", source"
                + " FROM " + RegionKeyspace.map(region,"account_shadow_attribute")
                + " WHERE account_id=?"
                + "   AND name=?"
                + "   AND value=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() || Region.GLOBAL == region ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId, name.name(), value);
        stmt.setIdempotent(true);
        return stmt;
    }
}
