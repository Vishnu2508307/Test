package com.smartsparrow.sso.data.oidc;

import static com.smartsparrow.dse.api.ResultSets.getNullableLong;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.sso.service.AccessToken;

class AccessTokenByWebSessionTokenMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccessTokenByWebSessionTokenMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(String webSessionToken) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  web_session_token"
                + ", id"
                + ", state"
                + ", relying_party_id"
                + ", access_token"
                + ", token_type"
                + ", expires_in"
                + " FROM iam_global.fidm_oidc_access_token_by_web_session_token"
                + " WHERE web_session_token = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(webSessionToken);
        stmt.setIdempotent(true);
        return stmt;
    }

    public AccessToken fromRow(Row row) {
        return new AccessToken() //
                .setWebSessionToken(row.getString("web_session_token")) //
                .setId(row.getUUID("id")) //
                .setState(row.getString("state")) //
                .setRelyingPartyId(row.getUUID("relying_party_id")) //
                .setAccessToken(row.getString("access_token")) //
                .setTokenType(row.getString("token_type")) //
                .setExpiresIn(getNullableLong(row, "expires_in"));
    }
}
