package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CoursewareConfigurationFieldMutator extends SimpleTableMutator<CoursewareElementConfigurationField> {

    @Override
    public String getUpsertQuery(CoursewareElementConfigurationField mutation) {
        return "INSERT INTO courseware.configuration_field_by_element (" +
                " element_id" +
                ", field_name" +
                ", field_value)" +
                " VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CoursewareElementConfigurationField mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getFieldName(),
                mutation.getFieldValue()
        );
    }
}
