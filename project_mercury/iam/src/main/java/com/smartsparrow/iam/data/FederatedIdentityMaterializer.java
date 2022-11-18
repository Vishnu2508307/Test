package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class FederatedIdentityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    FederatedIdentityMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByFederation(UUID subscriptionId, String clientId, String subjectId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  subscription_id"
                + ", client_id"
                + ", subject_id"
                + ", account_id"
                + " FROM iam_global.fidm_subject_account"
                + " WHERE subscription_id = ?"
                + "   AND client_id = ?"
                + "   AND subject_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(subscriptionId, clientId, subjectId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public FederatedIdentity fromRow(Row row) {
        return new FederatedIdentity()
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setClientId(row.getString("client_id"))
                .setSubjectId(row.getString("subject_id"))
                .setAccountId(row.getUUID("account_id"));
    }

}
