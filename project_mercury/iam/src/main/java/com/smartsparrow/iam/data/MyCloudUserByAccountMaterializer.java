package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class MyCloudUserByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public MyCloudUserByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findMyCloudUserId(final UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", mycloud_user_id"
                + " FROM iam_global.mycloud_user_by_account"
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public MyCloudAccountTracking fromRow(final Row row) {
        return new MyCloudAccountTracking()
                .setMyCloudUserId(row.getString("mycloud_user_id"))
                .setAccountId(row.getUUID("account_id"));
    }
}
