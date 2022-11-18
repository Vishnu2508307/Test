package com.smartsparrow.iam.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.AccountShadowAttribute;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.iam.service.Region;
import com.smartsparrow.util.Enums;

class AccountShadowAttributeMutator extends SimpleTableMutator<AccountShadowAttribute> {

    /**
     * This upsert "merges" the source maps.
     *
     * @param mutation
     * @return
     */
    @Override
    public String getUpsertQuery(AccountShadowAttribute mutation) {
        // @formatter:off
        return "UPDATE " + RegionKeyspace.map(mutation.getIamRegion(), "account_shadow_attribute")
                + "  SET iam_region = ?"
                + ", source = source + ?"
                + " WHERE account_id = ?"
                + "   AND name = ?"
                + "   AND value = ?";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountShadowAttribute mutation) {
        stmt.bind(mutation.getIamRegion().name(), Enums.asStringValues(mutation.getSource()), mutation.getAccountId(),
                  mutation.getAttribute().name(), mutation.getValue());
    }

    @Override
    public ConsistencyLevel upsertConsistencyLevel() {
        return isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM;
    }

    /**
     * Delete all the shadow attributes for the specified account in the specified region
     *
     * @param region the region
     * @param accountId the account
     * @return a statment to do so
     */
    public Statement deleteAll(Region region, UUID accountId) {
        final String QUERY = "DELETE FROM " + RegionKeyspace.map(region, "account_shadow_attribute") //
                + " WHERE account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Delete specified shadow attributes for the specified account in the specified region
     *
     * @param accountShadowAttributeName shadow attribute
     * @param region the region
     * @param accountId the account
     * @return a statement to do so
     */
    public Statement delete(Region region, UUID accountId, AccountShadowAttributeName accountShadowAttributeName) {
        final String QUERY = "DELETE FROM " + RegionKeyspace.map(region, "account_shadow_attribute") //
                + " WHERE account_id = ? AND name = ?";

        BoundStatement statement = stmtCache.asBoundStatement(QUERY);
        statement.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        statement.bind(accountId);
        statement.setString(1, accountShadowAttributeName.name());
        statement.setIdempotent(true);
        return statement;
    }
}
