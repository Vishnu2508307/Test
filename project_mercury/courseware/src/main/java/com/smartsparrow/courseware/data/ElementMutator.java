package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class ElementMutator extends SimpleTableMutator<CoursewareElement> {

    @Override
    public String getUpsertQuery(CoursewareElement mutation) {
        // @formatter:off
        return "INSERT INTO courseware.element ("
                + "  id"
                + ", element_type"
                + ") VALUES ( ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CoursewareElement mutation) {
        stmt.bind(
                mutation.getElementId(),
                Enums.asString(mutation.getElementType())
        );
    }
}
