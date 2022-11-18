package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class RequestNotificationByAssetMutator extends SimpleTableMutator<AssetRequestNotification> {

    @Override
    public String getUpsertQuery(AssetRequestNotification mutation) {
        return "INSERT INTO asset.request_notification_by_asset (" +
                "  asset_id" +
                ", notification_id" +
                ", original_height" +
                ", original_width" +
                ", threshold" +
                ", size" +
                ", url" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetRequestNotification mutation) {
        stmt.setUUID(0, mutation.getAssetId());
        stmt.setUUID(1, mutation.getNotificationId());
        stmt.setDouble(2, mutation.getOriginalHeight());
        stmt.setDouble(3, mutation.getOriginalWidth());
        stmt.setDouble(4, mutation.getThreshold());
        stmt.setString(5, mutation.getSize());
        Mutators.bindNonNull(stmt, 6, mutation.getUrl());
    }
}
