package com.smartsparrow.iam.data.permission.workspace;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.Row;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

class ThemePermissionByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ThemePermissionByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchThemePermissionByAccountTheme(UUID accountId, UUID themeId) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  account_id" +
                ", theme_id" +
                ", permission_level" +
                " FROM iam_global.theme_permission_by_account " +
                " WHERE account_id = ? AND theme_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(accountId, themeId);
        return stmt;
    }

    public ThemePermissionByAccount fromRow(Row row) {
        return new ThemePermissionByAccount()
                .setAccountId(row.getUUID("account_id"))
                .setThemeId(row.getUUID("theme_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));

    }

}
