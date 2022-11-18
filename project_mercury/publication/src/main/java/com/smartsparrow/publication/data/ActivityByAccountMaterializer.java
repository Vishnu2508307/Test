package com.smartsparrow.publication.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class ActivityByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ActivityByAccountMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID accountId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  account_id" +
                ", activity_id" +
                " FROM publication.activity_by_account" +
                " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(accountId);
        return stmt;
    }

    public ActivityByAccount fromRow(Row row) {
        return new ActivityByAccount()
                .setAccountId(row.getUUID("account_id"))
                .setActivityId(row.getUUID("activity_id"));
    }
}
