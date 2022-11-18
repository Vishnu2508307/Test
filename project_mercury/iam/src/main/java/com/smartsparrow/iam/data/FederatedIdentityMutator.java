package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class FederatedIdentityMutator extends SimpleTableMutator<FederatedIdentity> {

    @Override
    public String getUpsertQuery(FederatedIdentity mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_subject_account ("
                + "  subscription_id"
                + ", client_id"
                + ", subject_id"
                + ", account_id"
                + ") VALUES ( ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, FederatedIdentity mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getClientId(), mutation.getSubjectId(), mutation.getAccountId());
    }

}
