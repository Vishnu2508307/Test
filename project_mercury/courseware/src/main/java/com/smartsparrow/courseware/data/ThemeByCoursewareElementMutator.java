package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class ThemeByCoursewareElementMutator extends SimpleTableMutator<ThemeCoursewareElement> {

    @Override
    public String getUpsertQuery(ThemeCoursewareElement mutation) {
        // @formatter:off
        return "INSERT INTO courseware.theme_by_element ("
                + "  element_id"
                + ", theme_id"
                + ", element_type"
                + ") VALUES ( ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public String getDeleteQuery(ThemeCoursewareElement mutation) {
        return "DELETE FROM courseware.theme_by_element " +
                "WHERE element_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ThemeCoursewareElement mutation) {
        stmt.bind(mutation.getElementId(),
                  mutation.getThemeId(),
                  Enums.asString(mutation.getElementType()));
    }

    @Override
    public void bindDelete(BoundStatement stmt, ThemeCoursewareElement mutation) {
        stmt.bind(mutation.getElementId());
    }
}
