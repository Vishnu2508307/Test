package com.smartsparrow.sso.data.oidc;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.sso.service.OpenIDConnectState;

class StateMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    StateMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(String state) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  state"
                + ", redirect_url"
                + ", relying_party_id"
                + ", nonce"
                + " FROM iam_global.fidm_oidc_state"
                + " WHERE state = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(state);
        stmt.setIdempotent(true);
        return stmt;
    }

    public OpenIDConnectState fromRow(Row row) {
        return new OpenIDConnectState() //
                .setState(row.getString("state")) //
                .setRedirectUrl(row.getString("redirect_url")) //
                .setRelyingPartyId(row.getUUID("relying_party_id")) //
                .setNonce(row.getString("nonce"));
    }
}
