package com.smartsparrow.math.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.asset.data.AssetResultNotification;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

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
                ", math_ml" +
                ", svg_shape" +
                " FROM math.asset_result_notification_by_asset" +
                " WHERE asset_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(assetId);
        return stmt;
    }

    public MathAssetResultNotification fromRow(Row row) {
        return new MathAssetResultNotification()
                .setNotificationId(row.getUUID("notification_id"))
                .setAssetId(row.getUUID("asset_id"))
                .setMathML(row.getString("math_ml"))
                .setSvgShape(row.getString("svg_shape"));
    }
}
