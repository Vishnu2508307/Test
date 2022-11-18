package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class CoursewareElementByAssetUrnMutator extends SimpleTableMutator<CoursewareElementByAssetUrn> {

    @Override
    public String getUpsertQuery(CoursewareElementByAssetUrn mutation) {
        return "INSERT INTO courseware.courseware_by_asset_urn (" +
                " asset_urn" +
                ", element_id" +
                ", element_type) VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CoursewareElementByAssetUrn mutation) {
        stmt.bind(
                mutation.getAssetUrn(),
                mutation.getCoursewareElement().getElementId(),
                Enums.asString(mutation.getCoursewareElement().getElementType())
        );
    }

    @Override
    public String getDeleteQuery(CoursewareElementByAssetUrn mutation) {
        return "DELETE FROM courseware.courseware_by_asset_urn" +
                " WHERE asset_urn = ?" +
                " AND element_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, CoursewareElementByAssetUrn mutation) {
        stmt.bind(
                mutation.getAssetUrn(),
                mutation.getCoursewareElement().getElementId()
        );
    }
}
