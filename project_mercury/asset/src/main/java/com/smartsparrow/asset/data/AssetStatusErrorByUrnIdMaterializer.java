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

class AssetStatusErrorByUrnIdMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AssetStatusErrorByUrnIdMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAssetStatusError( final String assetUrn, final UUID assetId) {
        final String QUERY = "SELECT" +
                "  id" +
                ", asset_urn" +
                ", asset_id" +
                ", status" +
                ", error_cause" +
                ", error_message" +
                " FROM asset.status_error_by_urn_id" +
                " WHERE asset_urn = ? AND asset_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetUrn, assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public AssetStatusErrorByUrnId fromRow(final Row row) {
        return new AssetStatusErrorByUrnId()
                .setId(row.getUUID("id"))
                .setAssetUrn(row.getString("asset_urn"))
                .setAssetId(row.getUUID("asset_id"))
                .setStatus(Enums.of(AssetStatus.class, row.getString("status")))
                .setErrorCause(row.getString("error_cause"))
                .setErrorMessage(row.getString("error_message"));

    }
}
