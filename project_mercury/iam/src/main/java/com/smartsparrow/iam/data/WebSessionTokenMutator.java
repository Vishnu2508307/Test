package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.WebSessionToken;

public class WebSessionTokenMutator extends SimpleTableMutator<WebSessionToken> {

    @Override
    public String getUpsertQuery(WebSessionToken mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.web_session_token ("
                                                + "  key"
                                                + ", account_id"
                                                + ", created_ts"
                                                + ", expired_ts"
                                                + ", authority_subscription_id"
                                                + ", authority_relying_party_id"
                                                + ") VALUES ( ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, WebSessionToken mutation) {
        stmt.bind(mutation.getToken(), mutation.getAccountId(), mutation.getCreatedTs(), mutation.getValidUntilTs(),
                  mutation.getAuthoritySubscriptionId(), mutation.getAuthorityRelyingPartyId());
    }
}
