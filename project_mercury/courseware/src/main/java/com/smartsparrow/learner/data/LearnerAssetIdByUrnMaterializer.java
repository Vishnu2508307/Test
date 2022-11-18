package com.smartsparrow.learner.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LearnerAssetIdByUrnMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerAssetIdByUrnMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAssetId(final String assetUrn) {
        final String QUERY = "SELECT" +
                " asset_urn" +
                ", asset_id" +
                " FROM learner.asset_id_by_urn" +
                " WHERE asset_urn = ?" +
                " LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(assetUrn);
        return stmt;
    }

    public AssetIdByUrn fromRow(final Row row) {
        return new AssetIdByUrn()
                .setAssetUrn(row.getString("asset_urn"))
                .setAssetId(row.getUUID("asset_id"));
    }
}
