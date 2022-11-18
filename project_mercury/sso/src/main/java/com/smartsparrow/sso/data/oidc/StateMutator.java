package com.smartsparrow.sso.data.oidc;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.OpenIDConnectState;

class StateMutator extends SimpleTableMutator<OpenIDConnectState> {

    @Override
    public String getUpsertQuery(OpenIDConnectState mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_oidc_state ("
                + "  state"
                + ", redirect_url"
                + ", relying_party_id"
                + ", nonce"
                + ") VALUES ( ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, OpenIDConnectState mutation) {
        stmt.bind(mutation.getState(), //
                  mutation.getRedirectUrl(), //
                  mutation.getRelyingPartyId(), //
                  mutation.getNonce());
    }

}
