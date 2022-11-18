package com.smartsparrow.math.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class AssetByHashMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AssetByHashMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByHash(String hash) {
        final String SELECT = "SELECT " +
                "hash, " +
                "asset_id, " +
                "owner_id " +
                "FROM math.asset_by_hash " +
                "WHERE hash = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(hash);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to an asset by hash object
     */
    public AssetByHash fromRow(Row row) {
        return new AssetByHash()
                .setHash(row.getString("hash"))
                .setAssetId(row.getUUID("asset_id"))
                .setOwnerId(row.getUUID("owner_id"));
    }
}
