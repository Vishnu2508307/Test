package com.smartsparrow.asset.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

@Deprecated
public class MetadataByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public MetadataByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAllBy(UUID assetId) {
        final String SELECT = "SELECT " +
                "asset_id, " +
                "key, " +
                "value " +
                "FROM asset.metadata_by_asset " +
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
