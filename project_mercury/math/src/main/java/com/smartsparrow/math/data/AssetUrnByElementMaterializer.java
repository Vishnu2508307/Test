package com.smartsparrow.math.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class AssetUrnByElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AssetUrnByElementMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAssetUrnFor(final UUID elementId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " element_id,"
                + " asset_urn"
                + " FROM math.asset_urn_by_element"
                + " WHERE element_id = ?";
        // @formatter:on
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId);
        return stmt;
    }

    public AssetUrnByElement fromRow(final Row row) {
        return new AssetUrnByElement()
                .setAssetUrn(row.getString("asset_urn"))
                .setElementId(row.getUUID("element_id"));
    }
}
