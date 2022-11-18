package com.smartsparrow.asset.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ErrorNotificationByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ErrorNotificationByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID assetId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  asset_id" +
                ", notification_id" +
                ", cause" +
                ", error_message" +
                " FROM asset.error_notification_by_asset" +
                " WHERE asset_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(assetId);
        return stmt;
    }

    public AssetErrorNotification fromRow(Row row) {
        return new AssetErrorNotification()
                .setNotificationId(row.getUUID("notification_id"))
                .setErrorMessage(row.getString("error_message"))
                .setCause(row.getString("cause"))
                .setAssetId(row.getUUID("asset_id"));
    }
}
