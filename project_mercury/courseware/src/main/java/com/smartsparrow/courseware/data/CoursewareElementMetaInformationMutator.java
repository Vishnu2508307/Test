package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CoursewareElementMetaInformationMutator extends SimpleTableMutator<CoursewareElementMetaInformation> {

    @Override
    public String getUpsertQuery(final CoursewareElementMetaInformation mutation) {
        return "INSERT INTO courseware.courseware_element_meta_information (" +
                " element_id" +
                ", key" +
                ", value" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final CoursewareElementMetaInformation mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getKey(),
                mutation.getValue()
        );
    }
}
