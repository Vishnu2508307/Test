package com.smartsparrow.annotation.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class CoursewareAnnotationByMotivationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    CoursewareAnnotationByMotivationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetch(UUID rootElementId, Motivation motivation) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  annotation_id"
                + " FROM courseware.annotation_by_motivation"
                + " WHERE root_element_id=?"
                + "   AND motivation=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(rootElementId, Enums.asString(motivation));
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetch(UUID rootElementId, UUID elementId, Motivation motivation) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  annotation_id"
                + " FROM courseware.annotation_by_motivation"
                + " WHERE root_element_id=?"
                + "   AND element_id=?"
                + "   AND motivation=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(rootElementId, elementId, Enums.asString(motivation));
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("annotation_id");
    }

}
