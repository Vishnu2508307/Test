package com.smartsparrow.math.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class AssetUrnByElementMutator extends SimpleTableMutator<AssetUrnByElement> {

    @Override
    public String getUpsertQuery(AssetUrnByElement mutation) {
        return "INSERT INTO math.asset_urn_by_element (" +
                "element_id" +
                ", asset_urn" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetUrnByElement mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getAssetUrn());
    }

    @Override
    public String getDeleteQuery(AssetUrnByElement mutation) {
        return "DELETE FROM math.asset_urn_by_element" +
                " WHERE element_id = ?" +
                " AND asset_urn = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, AssetUrnByElement mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getAssetUrn()
        );
    }
}
