package com.smartsparrow.math.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ErrorNotificationByAssetMutator extends SimpleTableMutator<MathAssetErrorNotification> {

    @Override
    public String getUpsertQuery(MathAssetErrorNotification mutation) {
        return "INSERT INTO math.error_notification_by_asset (" +
                " asset_id" +
                ", notification_id" +
                ", cause" +
                ", error_message" +
                ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, MathAssetErrorNotification mutation) {
        stmt.setUUID(0, mutation.getAssetId());
        stmt.setUUID(1, mutation.getNotificationId());
        stmt.setString(2, mutation.getCause());
        stmt.setString(3, mutation.getErrorMessage());
    }
}
