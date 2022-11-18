package com.smartsparrow.iam.data;


import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.util.Enums;

class CredentialByHashMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CredentialByHashMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchCredential(final String hash) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  hash" +
                ", credential_type" +
                ", account_id" +
                " FROM iam_global.credential_type_by_hash " +
                " WHERE hash = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(hash);
        return stmt;
    }

    public CredentialsType fromRow(Row row) {
        return new CredentialsType()
                .setHash(row.getString("hash"))
                .setAuthenticationType(Enums.of(AuthenticationType.class, row.getString("credential_type")))
                .setAccountId(row.getUUID("account_id"));
    }

}
