package com.smartsparrow.sso.data.oidc;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.AccessToken;

class AccessTokenByWebSessionTokenMutator extends SimpleTableMutator<AccessToken> {

    @Override
    public String getUpsertQuery(AccessToken mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_oidc_access_token_by_web_session_token ("
                + "  web_session_token"
                + ", id"
                + ", state"
                + ", relying_party_id"
                + ", access_token"
                + ", token_type"
                + ", expires_in"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccessToken mutation) {
        stmt.bind(mutation.getWebSessionToken(), //
                  mutation.getId(), //
                  mutation.getState(), //
                  mutation.getRelyingPartyId(), //
                  mutation.getAccessToken(), //
                  mutation.getTokenType(), //
                  mutation.getExpiresIn());
    }

}
