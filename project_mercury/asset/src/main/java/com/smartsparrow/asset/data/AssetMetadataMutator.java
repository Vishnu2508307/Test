package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class AssetMetadataMutator extends SimpleTableMutator<AssetMetadata> {

    @Override
    public String getUpsertQuery(AssetMetadata mutation) {
        return "INSERT INTO asset.asset_metadata (" +
                "asset_id, " +
                "key, " +
                "value) " +
                "VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetMetadata mutation) {
        stmt.bind(
                mutation.getAssetId(),
                mutation.getKey(),
                mutation.getValue()
        );
    }
}
