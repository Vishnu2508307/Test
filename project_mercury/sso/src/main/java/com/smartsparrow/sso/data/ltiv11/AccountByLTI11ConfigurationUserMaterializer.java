package com.smartsparrow.sso.data.ltiv11;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class AccountByLTI11ConfigurationUserMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AccountByLTI11ConfigurationUserMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByConfigurationUser(final UUID configurationId, final String userId) {
        final String SELECT = "SELECT" +
                " account_id" +
                " FROM iam_global.account_by_lti11_configuration_user" +
                " WHERE consumer_configuration_id = ?" +
                " AND user_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(configurationId, userId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(final Row row) {
        return row.getUUID("account_id");
    }
}
