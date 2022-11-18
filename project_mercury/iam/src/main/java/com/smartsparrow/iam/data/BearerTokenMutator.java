package com.smartsparrow.iam.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.BearerToken;

public class BearerTokenMutator extends SimpleTableMutator<BearerToken> {

    private static final Logger log = LoggerFactory.getLogger(BearerTokenMutator.class);

    @Override
    public String getUpsertQuery(BearerToken mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.bearer_token ("
                                                + "  key"
                                                + ", account_id"
                                                + ") VALUES ( ?, ?)"
                                                + " USING TTL ?";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, BearerToken mutation) {
        stmt.bind(mutation.getToken(), mutation.getAccountId(), 0);
    }

    /**
     * Creation of an upsert (insert/update) statement with ttl.
     *
     * @param mutation bearer token to save
     * @param ttl time to live in seconds
     * @return upsert statement
     */
    public Statement upsert(BearerToken mutation, int ttl) {
        String upsertQuery = getUpsertQuery(mutation);
        if (log.isDebugEnabled()) {
            log.debug("upsert: {}", upsertQuery);
        }

        BoundStatement stmt = stmtCache.asBoundStatement(upsertQuery);
        stmt.setConsistencyLevel(upsertConsistencyLevel());
        stmt.setIdempotent(isUpsertIdempotent());
        stmt.bind(mutation.getToken(), mutation.getAccountId(), ttl);

        return stmt;
    }

    @Override
    public String getDeleteQuery(BearerToken mutation) {
        // @formatter:off
        return "DELETE FROM iam_global.bearer_token"
                + "  WHERE key = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, BearerToken mutation) {
        stmt.bind(mutation.getToken());
    }
}
