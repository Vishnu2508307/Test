package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.Claim;

class ClaimNameBySubscriptionMutator extends SimpleTableMutator<Claim> {

    @Override
    public String getUpsertQuery(Claim mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.claim_name_by_subscription ("
                + "  subscription_id"
                + ", name"
                + ") VALUES ( ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Claim mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getName());
    }

}
