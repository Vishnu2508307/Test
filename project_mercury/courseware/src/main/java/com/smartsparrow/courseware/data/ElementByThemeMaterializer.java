package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class ElementByThemeMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ElementByThemeMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchByThemeId(final UUID themeId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  theme_id"
                + " , element_id"
                + " FROM courseware.elements_by_theme"
                + " WHERE theme_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId);
        return stmt;
    }

    public ThemeCoursewareElement fromRow(Row row) {
        return new ThemeCoursewareElement()
                .setThemeId(row.getUUID("theme_id"))
                .setElementId(row.getUUID("element_id"));
    }
}
