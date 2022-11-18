package com.smartsparrow.asset.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class ResultNotificationByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ResultNotificationByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID assetId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  asset_id" +
                ", notification_id" +
                ", height" +
                ", width" +
                ", size" +
                ", url" +
                " FROM asset.result_notification_by_asset" +
                " WHERE asset_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(assetId);
        return stmt;
    }

    public AssetResultNotification fromRow(Row row) {
        return new AssetResultNotification()
                .setNotificationId(row.getUUID("notification_id"))
                .setHeight(row.getDouble("height"))
                .setWidth(row.getDouble("width"))
                .setSize(row.getString("size"))
                .setUrl(row.getString("url"))
                .setAssetId(row.getUUID("asset_id"));
    }
}
