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

public class AssetSummaryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AssetSummaryMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAssetBy(UUID id) {
        final String SELECT = "SELECT " +
                "id, " +
                "urn, " +
                "provider, " +
                "owner_id, " +
                "subscription_id, " +
                "media_type, " +
                "hash, " +
                "visibility " +
                "FROM asset.summary " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to an asset summary object
     */
    public AssetSummary fromRow(Row row) {
        return new AssetSummary()
                .setUrn(row.getString("urn"))
                .setId(row.getUUID("id"))
                .setProvider(Enums.of(AssetProvider.class, row.getString("provider")))
                .setOwnerId(row.getUUID("owner_id"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setMediaType(Enums.of(AssetMediaType.class, row.getString("media_type")))
                .setHash(row.getString("hash"))
                .setVisibility(Enums.of(AssetVisibility.class, row.getString("visibility")));
    }
}
