package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

import javax.inject.Inject;
import java.util.UUID;

public class CoursewareElementDescriptionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CoursewareElementDescriptionMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID elementId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " element_id,"
                + " element_type,"
                + " value"
                + " FROM courseware.element_description"
                + " WHERE element_id = ?";
        // @formatter:on
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId);
        return stmt;
    }

    public CoursewareElementDescription fromRow(Row row) {
        return new CoursewareElementDescription()
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setValue(row.getString("value"));
    }
}
