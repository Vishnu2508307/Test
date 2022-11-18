package com.smartsparrow.sso.data.oidc;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.sso.service.OpenIDConnectRelyingPartyCredential;

class RelyingPartyCredentialMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    RelyingPartyCredentialMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(UUID relyingPartyId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  relying_party_id"
                + ", subscription_id"
                + ", issuer_url"
                + ", client_id"
                + ", client_secret"
                + ", authentication_request_scope"
                + ", log_debug"
                + ", enforce_verified_email"
                + " FROM iam_global.fidm_oidc_rp_credential"
                + " WHERE relying_party_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(relyingPartyId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public OpenIDConnectRelyingPartyCredential fromRow(Row row) {
        return new OpenIDConnectRelyingPartyCredential() //
                .setRelyingPartyId(row.getUUID("relying_party_id")) //
                .setSubscriptionId(row.getUUID("subscription_id")) //
                .setIssuerUrl(row.getString("issuer_url")) //
                .setClientId(row.getString("client_id")) //
                .setClientSecret(row.getString("client_secret")) //
                .setAuthenticationRequestScope(row.getString("authentication_request_scope"))
                .setLogDebug(row.getBool("log_debug"))
                .setEnforceVerifiedEmail(row.getBool("enforce_verified_email"));
    }
}
