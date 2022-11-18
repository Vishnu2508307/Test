package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class AssetUrnByCoursewareMutator extends SimpleTableMutator<CoursewareElementByAssetUrn> {

    @Override
    public String getUpsertQuery(CoursewareElementByAssetUrn mutation) {
        return "INSERT INTO courseware.asset_urn_by_courseware (" +
                " element_id" +
                ", asset_urn" +
                ", element_type) VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CoursewareElementByAssetUrn mutation) {
        stmt.bind(
                mutation.getCoursewareElement().getElementId(),
                mutation.getAssetUrn(),
                Enums.asString(mutation.getCoursewareElement().getElementType())
        );
    }

    @Override
    public String getDeleteQuery(CoursewareElementByAssetUrn mutation) {
        return "DELETE FROM courseware.asset_urn_by_courseware" +
                " WHERE element_id = ?" +
                " AND asset_urn = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, CoursewareElementByAssetUrn mutation) {
        stmt.bind(
                mutation.getCoursewareElement().getElementId(),
                mutation.getAssetUrn()
        );
    }
}
