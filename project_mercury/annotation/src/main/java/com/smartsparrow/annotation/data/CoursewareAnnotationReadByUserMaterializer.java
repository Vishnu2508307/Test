package com.smartsparrow.annotation.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.annotation.service.CoursewareAnnotationReadByUser;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class CoursewareAnnotationReadByUserMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    CoursewareAnnotationReadByUserMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetch(UUID rootElementId, UUID elementId, UUID annotationId, UUID userId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  annotation_id"
                + ", user_id"
                + " FROM courseware.annotation_read_by_user"
                + " WHERE root_element_id = ? "
                + " AND element_id = ? "
                + " AND annotation_id = ? "
                + " AND user_id = ? "
                + " LIMIT 1";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(rootElementId, elementId, annotationId, userId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public CoursewareAnnotationReadByUser fromRow(Row row) {
        return new CoursewareAnnotationReadByUser()
                .setAnnotationId(row.getUUID("annotation_id"))
                .setUserId(row.getUUID("user_id"));
    }
}
