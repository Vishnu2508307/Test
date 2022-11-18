package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class AssetStatusByUrnMutator extends SimpleTableMutator<AssetStatusByUrn> {

    @Override
    public String getUpsertQuery(AssetStatusByUrn mutation) {
        // @formatter:off
        return "INSERT INTO asset.status_by_urn (" +
                "  id" +
                ", asset_urn" +
                ", asset_id" +
                ", status" +
                ") VALUES (?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetStatusByUrn mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setString(1, mutation.getAssetUrn());
        stmt.setUUID(2, mutation.getAssetId());
        stmt.setString(3, Enums.asString(mutation.getStatus()));
    }
}
