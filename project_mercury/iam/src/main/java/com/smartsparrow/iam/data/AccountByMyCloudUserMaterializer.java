package com.smartsparrow.iam.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class AccountByMyCloudUserMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AccountByMyCloudUserMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAccountId(final String iesUserId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  mycloud_user_id"
                + ", account_id"
                + " FROM iam_global.account_by_mycloud_user"
                + " WHERE mycloud_user_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(iesUserId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public MyCloudAccountTracking fromRow(final Row row) {
        return new MyCloudAccountTracking()
                .setAccountId(row.getUUID("account_id"))
                .setMyCloudUserId(row.getString("mycloud_user_id"));
    }
}
