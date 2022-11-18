package com.smartsparrow.math.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AssetByHashMutator extends SimpleTableMutator<AssetByHash> {

    @Override
    public String getUpsertQuery(AssetByHash mutation) {
        return "INSERT INTO math.asset_by_hash (" +
                "hash, " +
                "asset_id, " +
                "owner_id) " +
                "VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetByHash mutation) {
        stmt.bind(
                mutation.getHash(),
                mutation.getAssetId(),
                mutation.getOwnerId()
        );
    }
}
