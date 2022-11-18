package com.smartsparrow.sso.data.ltiv11;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.sso.service.LTIConsumerKey;

/**
 * This class has been deprecated and should not be used
 */
@Deprecated
class LTICredentialByKeyMaterializer {

    @Inject
    private PreparedStatementCache stmtCache;

    public Statement fetchByKey(String key) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  id"
                + ", oauth_consumer_key"
                + ", oauth_consumer_secret"
                + ", subscription_id"
                + ", comment"
                + ", log_debug"
                + " FROM iam_global.fidm_ltiv11_credential_by_key"
                + " WHERE oauth_consumer_key=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(key);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LTIConsumerKey fromRow(Row row) {
        return new LTIConsumerKey()
                .setId(row.getUUID("id"))
                .setKey(row.getString("oauth_consumer_key"))
                .setSecret(row.getString("oauth_consumer_secret"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setComment(row.getString("comment"))
                .setLogDebug(row.getBool("log_debug"));
    }
}
