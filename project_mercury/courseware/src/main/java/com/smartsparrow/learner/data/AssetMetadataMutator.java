package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AssetMetadataMutator extends SimpleTableMutator<AssetMetadata> {

    @Override
    public String getUpsertQuery(AssetMetadata mutation) {
        return "INSERT INTO learner.asset_metadata (" +
                "asset_id, " +
                "key, " +
                "value) " +
                "VALUES (?,?,?)";
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
