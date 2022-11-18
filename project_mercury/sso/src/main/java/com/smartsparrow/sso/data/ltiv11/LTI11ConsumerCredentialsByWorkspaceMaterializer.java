package com.smartsparrow.sso.data.ltiv11;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LTI11ConsumerCredentialsByWorkspaceMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    LTI11ConsumerCredentialsByWorkspaceMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByWorkspaceCohortKey(final UUID workspaceId, final UUID cohortId, final String key) {
        final String SELECT = "SELECT" +
                " workspace_id" +
                ", cohort_id" +
                ", oauth_consumer_key" +
                ", oauth_consumer_secret" +
                ", consumer_configuration_id" +
                ", id" +
                ", log_debug" +
                " FROM iam_global.lti11_consumer_credentials_by_workspace" +
                " WHERE workspace_id = ?" +
                " AND cohort_id = ?" +
                " AND oauth_consumer_key = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(workspaceId, cohortId, key);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findByWorkspaceCohort(final UUID workspaceId, final UUID cohortId) {
        final String SELECT = "SELECT" +
                " workspace_id" +
                ", cohort_id" +
                ", oauth_consumer_key" +
                ", oauth_consumer_secret" +
                ", consumer_configuration_id" +
                ", id" +
                ", log_debug" +
                " FROM iam_global.lti11_consumer_credentials_by_workspace" +
                " WHERE workspace_id = ?" +
                " AND cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(workspaceId, cohortId);
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
