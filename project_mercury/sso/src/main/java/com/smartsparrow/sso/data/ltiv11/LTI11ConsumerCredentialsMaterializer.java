package com.smartsparrow.sso.data.ltiv11;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LTI11ConsumerCredentialsMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LTI11ConsumerCredentialsMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID id) {
        final String SELECT = "SELECT" +
                " id" +
                ", oauth_consumer_key" +
                ", oauth_consumer_secret" +
                ", cohort_id" +
                ", workspace_id" +
                ", consumer_configuration_id" +
                ", log_debug" +
                " FROM iam_global.lti11_consumer_credentials" +
                " WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LTIv11ConsumerKey fromRow(final Row row) {
        return new LTIv11ConsumerKey()
                .setId(row.getUUID("id"))
                .setOauthConsumerKey(row.getString("oauth_consumer_key"))
                .setOauthConsumerSecret(row.getString("oauth_consumer_secret"))
                .setCohortId(row.getUUID("cohort_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setConsumerConfigurationId(row.getUUID("consumer_configuration_id"))
                .setLogDebug(row.getBool("log_debug"));
    }
}
