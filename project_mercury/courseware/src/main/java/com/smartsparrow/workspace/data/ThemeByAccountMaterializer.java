package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class ThemeByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ThemeByAccountMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchThemeForAccount(final UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  account_id"
                + " , theme_id"
                + " FROM workspace.theme_by_account"
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(accountId);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("theme_id");
    }
}
