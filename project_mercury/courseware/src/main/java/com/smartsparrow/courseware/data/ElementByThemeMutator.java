package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ElementByThemeMutator extends SimpleTableMutator<ThemeCoursewareElement> {

    @Override
    public String getUpsertQuery(ThemeCoursewareElement mutation) {
        // @formatter:off
        return "INSERT INTO courseware.elements_by_theme ("
                + "  theme_id"
                + ", element_id"
                + ") VALUES ( ?, ?)";
        // @formatter:on
    }

    @Override
    public String getDeleteQuery(ThemeCoursewareElement mutation) {
        return "DELETE FROM courseware.elements_by_theme " +
                "WHERE theme_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ThemeCoursewareElement mutation) {
        stmt.bind( mutation.getThemeId(),
                   mutation.getElementId());
    }

    @Override
    public void bindDelete(BoundStatement stmt, ThemeCoursewareElement mutation) {
        stmt.bind(mutation.getThemeId());
    }

    public Statement deleteElementsByTheme(UUID themeId, UUID elementId) {
        String deleteQuery = "DELETE FROM courseware.elements_by_theme"
                + " WHERE theme_id = ?"
                + " AND element_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(deleteQuery);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(themeId, elementId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
