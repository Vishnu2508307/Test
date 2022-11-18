package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ResultNotificationByAssetMutator extends SimpleTableMutator<AssetResultNotification> {

    @Override
    public String getUpsertQuery(AssetResultNotification mutation) {
        return "INSERT INTO asset.result_notification_by_asset (" +
                "  asset_id" +
                ", notification_id" +
                ", height" +
                ", width" +
                ", size" +
                ", url" +
                ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetResultNotification mutation) {
        stmt.setUUID(0, mutation.getAssetId());
        stmt.setUUID(1, mutation.getNotificationId());
        stmt.setDouble(2, mutation.getHeight());
        stmt.setDouble(3, mutation.getWidth());
        stmt.setString(4, mutation.getSize());
        Mutators.bindNonNull(stmt, 5, mutation.getUrl());
    }
}
