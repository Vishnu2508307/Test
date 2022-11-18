package com.smartsparrow.sso.data.oidc;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.OpenIDConnectRelyingPartyCredential;

class RelyingPartyCredentialMutator extends SimpleTableMutator<OpenIDConnectRelyingPartyCredential> {

    @Override
    public String getUpsertQuery(OpenIDConnectRelyingPartyCredential mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_oidc_rp_credential ("
                + "  relying_party_id"
                + ", subscription_id"
                + ", issuer_url"
                + ", client_id"
                + ", client_secret"
                + ", authentication_request_scope"
                + ", log_debug"
                + ", enforce_verified_email"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, OpenIDConnectRelyingPartyCredential mutation) {
        stmt.bind(mutation.getRelyingPartyId(), //
                  mutation.getSubscriptionId(), //
                  mutation.getIssuerUrl(), //
                  mutation.getClientId(), //
                  mutation.getClientSecret(), //
                  mutation.getAuthenticationRequestScope(),
                  mutation.isLogDebug(),
                  mutation.isEnforceVerifiedEmail());
    }

}
