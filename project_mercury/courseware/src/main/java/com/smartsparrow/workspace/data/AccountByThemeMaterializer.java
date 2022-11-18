package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

class AccountByThemeMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    AccountByThemeMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchAccountsForTheme(final UUID themeId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  theme_id"
                + ", account_id"
                + ", permission_level"
                + " FROM workspace.account_by_theme"
                + " WHERE theme_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId);
        return stmt;
    }

    public AccountByTheme fromRow(Row row) {
        return new AccountByTheme()
                .setThemeId(row.getUUID("theme_id"))
                .setAccountId(row.getUUID("account_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
