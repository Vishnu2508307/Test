package com.smartsparrow.asset.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class AssetStatusByUrnMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AssetStatusByUrnMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAssetStatusByUrn(final String assetUrn) {
        final String QUERY = "SELECT" +
                "  id" +
                ", asset_urn" +
                ", asset_id" +
                ", status" +
                " FROM asset.status_by_urn" +
                " WHERE asset_urn = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetUrn);
        stmt.setIdempotent(true);
        return stmt;
    }

    public AssetStatusByUrn fromRow(final Row row) {
        return new AssetStatusByUrn()
                .setId(row.getUUID("id"))
                .setAssetUrn(row.getString("asset_urn"))
                .setAssetId(row.getUUID("asset_id"))
                .setStatus(Enums.of(AssetStatus.class, row.getString("status")));

    }
}
