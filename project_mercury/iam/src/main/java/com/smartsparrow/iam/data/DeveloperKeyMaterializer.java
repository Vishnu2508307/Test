package com.smartsparrow.iam.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DeveloperKeyMaterializer implements TableMaterializer {

    private PreparedStatementCache stmtCache;

    @Inject
    public DeveloperKeyMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByKey(String key) {
        final String QUERY = "SELECT " +
                "key, " +
                "subscription_id, " +
                "account_id, " +
                "created_ts " +
                "FROM iam_global.developer_key " +
                "WHERE key = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(key);
        stmt.setIdempotent(true);
        return stmt;
    }
}
