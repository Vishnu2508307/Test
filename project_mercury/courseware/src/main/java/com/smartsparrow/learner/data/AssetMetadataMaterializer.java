package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class AssetMetadataMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AssetMetadataMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAllBy(UUID assetId) {
        final String SELECT = "SELECT " +
                "asset_id, " +
                "key, " +
                "value " +
                "FROM learner.asset_metadata " +
                "WHERE asset_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to an metadata object
     */
    public AssetMetadata fromRow(Row row) {
        return new AssetMetadata()
                .setAssetId(row.getUUID("asset_id"))
                .setKey(row.getString("key"))
                .setValue(row.getString("value"));
    }
}
