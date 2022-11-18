package com.smartsparrow.sso.data.ltiv11;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class AccountByLTI11ConfigurationUserMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(final UUID consumerConfigurationId, final String userId, final UUID accountId) {
        final String QUERY = "INSERT INTO iam_global.account_by_lti11_configuration_user (" +
                " consumer_configuration_id" +
                ", user_id" +
                ", account_id) VALUES (?, ?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(consumerConfigurationId, userId, accountId);
        return stmt;
    }
}
