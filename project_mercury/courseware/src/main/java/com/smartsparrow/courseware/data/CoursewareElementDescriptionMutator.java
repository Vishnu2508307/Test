package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CoursewareElementDescriptionMutator extends SimpleTableMutator<CoursewareElementDescription> {

    public Statement upsert(CoursewareElementDescription element) {
        // @formatter:off
        String QUERY = "INSERT INTO courseware.element_description ("
                + "  element_id"
                + ", element_type"
                + ", value"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(element.getElementId(), element.getElementType().name(), element.getValue());
        stmt.setIdempotent(true);
        return stmt;
    }

}
