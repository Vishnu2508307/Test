package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class ThemeMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ThemeMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findThemeById(final UUID themeId) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  id" +
                ", name" +
                " FROM workspace.theme " +
                " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId);
        return stmt;
    }

    public Theme fromRow(Row row) {
        return new Theme()
                .setId(row.getUUID("id"))
                .setName(row.getString("name"));

    }
}
