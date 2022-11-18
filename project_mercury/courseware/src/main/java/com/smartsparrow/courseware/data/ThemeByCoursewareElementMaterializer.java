package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class ThemeByCoursewareElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ThemeByCoursewareElementMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchByElementId(final UUID elementId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  element_id"
                + " , theme_id"
                + " , element_type"
                + " FROM courseware.theme_by_element"
                + " WHERE element_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId);
        return stmt;
    }

    public ThemeCoursewareElement fromRow(Row row) {
        return new ThemeCoursewareElement()
                .setElementId(row.getUUID("element_id"))
                .setThemeId(row.getUUID("theme_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")));
    }
}
