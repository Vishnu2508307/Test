package com.smartsparrow.iam.data;


import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.util.Enums;

class CredentialsByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CredentialsByAccountMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchCredentials(final UUID accountId) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  hash" +
                ", credential_type" +
                ", account_id" +
                " FROM iam_global.credentials_type_by_account " +
                " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(accountId);
        return stmt;
    }

    public CredentialsType fromRow(Row row) {
        return new CredentialsType()
                .setHash(row.getString("hash"))
                .setAuthenticationType(Enums.of(AuthenticationType.class, row.getString("credential_type")))
                .setAccountId(row.getUUID("account_id"));
    }

}
