package com.smartsparrow.asset.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class AssetIdByUrnMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AssetIdByUrnMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAssetId(final String assetUrn) {
        final String QUERY = "SELECT" +
                " asset_urn" +
                ", asset_id" +
                " FROM asset.asset_id_by_urn" +
                " WHERE asset_urn = ?" +
                " LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetUrn);
        stmt.setIdempotent(true);
        return stmt;
    }

    public AssetIdByUrn fromRow(final Row row) {
        return new AssetIdByUrn()
                .setAssetId(row.getUUID("asset_id"))
                .setAssetUrn(row.getString("asset_urn"));
    }
}
