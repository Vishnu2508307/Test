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

class ElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ElementMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchById(final UUID elementId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", element_type"
                + " FROM courseware.element"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId);
        return stmt;
    }

    /**
     * Maps a row to a courseware element
     * @param row to be converted
     * @return materialized element object built from the row
     */
    public CoursewareElement fromRow(Row row) {
        return new CoursewareElement() //
                .setElementId(row.getUUID("id")) //
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")));
    }
}
