package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.Claim;

class ClaimByAccountMutator extends SimpleTableMutator<Claim> {

    @Override
    public String getUpsertQuery(Claim mutation) {
        // @formatter:off
        return "INSERT INTO " + RegionKeyspace.map(mutation.getIamRegion(), "claim_by_account") + " ("
                + "  account_id"
                + ", subscription_id"
                + ", name"
                + ", value"
                + ") VALUES ( ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Claim mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getSubscriptionId(), mutation.getName(), mutation.getValue());
    }

    @Override
    public ConsistencyLevel upsertConsistencyLevel() {
        return isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM;
    }

}
