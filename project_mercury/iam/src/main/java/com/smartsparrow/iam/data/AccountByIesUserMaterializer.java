package com.smartsparrow.iam.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class AccountByIesUserMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AccountByIesUserMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAccountId(final String iesUserId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  ies_user_id"
                + ", account_id"
                + " FROM iam_global.account_by_ies_user"
                + " WHERE ies_user_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(iesUserId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public IESAccountTracking fromRow(final Row row) {
        return new IESAccountTracking()
                .setAccountId(row.getUUID("account_id"))
                .setIesUserId(row.getString("ies_user_id"));
    }
}
