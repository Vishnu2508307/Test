package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class LearnerAssetIdByUrnMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(final String assetUrn, final UUID assetId) {
        final String QUERY = "INSERT INTO learner.asset_id_by_urn (" +
                " asset_urn" +
                ", asset_id) VALUES (?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(assetUrn, assetId);
        return stmt;
    }
}
