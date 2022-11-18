package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

@Deprecated
public class MetadataByAssetMutator extends SimpleTableMutator<AssetMetadata> {

    @Override
    public String getUpsertQuery(AssetMetadata mutation) {
        return "INSERT INTO asset.metadata_by_asset (" +
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
