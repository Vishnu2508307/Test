package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ErrorNotificationByAssetMutator extends SimpleTableMutator<AssetErrorNotification> {

    @Override
    public String getUpsertQuery(AssetErrorNotification mutation) {
        return "INSERT INTO asset.error_notification_by_asset (" +
                " asset_id" +
                ", notification_id" +
                ", cause" +
                ", error_message" +
                ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetErrorNotification mutation) {
        stmt.setUUID(0, mutation.getAssetId());
        stmt.setUUID(1, mutation.getNotificationId());
        stmt.setString(2, mutation.getCause());
        stmt.setString(3, mutation.getErrorMessage());
    }
}
