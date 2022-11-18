package com.smartsparrow.math.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.asset.data.AssetRequestNotification;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class RequestNotificationByAssetMutator extends SimpleTableMutator<MathAssetRequestNotification> {

    @Override
    public String getUpsertQuery(MathAssetRequestNotification mutation) {
        return "INSERT INTO math.asset_request_notification_by_asset (" +
                "  asset_id" +
                ", notification_id" +
                ", math_ml" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, MathAssetRequestNotification mutation) {
        stmt.setUUID(0, mutation.getAssetId());
        stmt.setUUID(1, mutation.getNotificationId());
        Mutators.bindNonNull(stmt, 2, mutation.getMathML());
    }
}
