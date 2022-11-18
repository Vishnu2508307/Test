package com.smartsparrow.asset.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class AssetIdByUrnMutator extends SimpleTableMutator<UUID> {

    public Statement persist(final String assetUrn, final UUID assetId) {
        final String QUERY = "INSERT INTO asset.asset_id_by_urn (" +
                " asset_urn" +
                ", asset_id" +
                ") VALUES (?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetUrn, assetId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
