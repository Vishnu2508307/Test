package com.smartsparrow.math.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ResultNotificationByAssetMutator extends SimpleTableMutator<MathAssetResultNotification> {

    @Override
    public String getUpsertQuery(MathAssetResultNotification mutation) {
        return "INSERT INTO math.asset_result_notification_by_asset (" +
                "  asset_id" +
                ", notification_id" +
                ", math_ml" +
                ", svg_shape" +
                ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, MathAssetResultNotification mutation) {
        stmt.setUUID(0, mutation.getAssetId());
        stmt.setUUID(1, mutation.getNotificationId());
        stmt.setString(2, mutation.getMathML());
        stmt.setString(3, mutation.getSvgShape());
    }
}
