package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class AssetStatusErrorByUrnIdMutator extends SimpleTableMutator<AssetStatusErrorByUrnId> {

    @Override
    public String getUpsertQuery(AssetStatusErrorByUrnId mutation) {
        // @formatter:off
        return  "INSERT INTO asset.status_error_by_urn_id (" +
                "  id" +
                ", asset_urn" +
                ", asset_id" +
                ", status" +
                ", error_cause" +
                ", error_message" +
                ") VALUES (?, ?, ?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetStatusErrorByUrnId mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setString(1, mutation.getAssetUrn());
        stmt.setUUID(2, mutation.getAssetId());
        stmt.setString(3, Enums.asString(mutation.getStatus()));
        stmt.setString(4, mutation.getErrorCause());
        stmt.setString(5, mutation.getErrorMessage());
    }
}
