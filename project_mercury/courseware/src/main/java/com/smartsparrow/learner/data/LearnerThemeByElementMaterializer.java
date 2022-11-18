package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LearnerThemeByElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerThemeByElementMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findThemeByElementId(final UUID themeId) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  element_id" +
                ", theme_id" +
                ", theme_name" +
                " FROM learner.theme_by_element " +
                " WHERE element_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId);
        return stmt;
    }

    public LearnerThemeByElement fromRow(Row row) {
        return new LearnerThemeByElement()
                .setElementId(row.getUUID("element_id"))
                .setThemeId(row.getUUID("theme_id"))
                .setThemeName(row.getString("theme_name"));

    }
}
