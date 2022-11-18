package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class AssetUrnByRootActivityMutator extends SimpleTableMutator<AssetUrnByRootActivity> {

    @Override
    public String getUpsertQuery(AssetUrnByRootActivity mutation) {
        return "INSERT INTO courseware.asset_urn_by_root_activity (" +
                " root_activity_id" +
                ", asset_urn" +
                ", element_id" +
                ", element_type" +
                ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetUrnByRootActivity mutation) {
        stmt.bind(
                mutation.getRootActivityId(),
                mutation.getAssetUrn(),
                mutation.getCoursewareElement().getElementId(),
                Enums.asString(mutation.getCoursewareElement().getElementType())
        );
    }

    @Override
    public String getDeleteQuery(AssetUrnByRootActivity mutation) {
        return "DELETE FROM courseware.asset_urn_by_root_activity" +
                " WHERE root_activity_id = ?" +
                " AND asset_urn = ?" +
                " AND element_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, AssetUrnByRootActivity mutation) {
        stmt.bind(
                mutation.getRootActivityId(),
                mutation.getAssetUrn(),
                mutation.getCoursewareElement().getElementId()
        );
    }
}
