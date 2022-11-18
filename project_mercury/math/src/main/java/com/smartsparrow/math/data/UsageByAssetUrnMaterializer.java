package com.smartsparrow.math.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class UsageByAssetUrnMaterializer implements TableMaterializer {
    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public UsageByAssetUrnMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findByUrn(String urn) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  asset_urn"
                + ", asset_id"
                + ", element_id"
                + " FROM math.usage_by_asset_urn"
                + " WHERE asset_urn = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(urn);
        return stmt;
    }

    public UsageByAssetUrn fromRow(Row row) {
        return new UsageByAssetUrn()
                .setAssetId(row.getUUID("asset_id"))
                .setAssetUrn(row.getString("asset_urn"))
                .setElementId(row.getList("element_id", String.class));
    }
}
