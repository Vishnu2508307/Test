package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class LearnerAssetUrnByCoursewareMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(final UUID elementId, final UUID changeId, final String assetUrn) {
        // @formatter:off
        String QUERY = "INSERT INTO learner.asset_urn_by_courseware ("
                + "  element_id"
                + ", change_id"
                + ", asset_urn"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(elementId, changeId, assetUrn);
        stmt.setIdempotent(true);
        return stmt;
    }
}
