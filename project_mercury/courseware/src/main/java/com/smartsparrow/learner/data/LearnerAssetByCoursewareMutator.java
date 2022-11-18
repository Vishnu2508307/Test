package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

/**
 * Being replaced by asset urn tracking (part of immutable asset urn refactoring)
 */
@Deprecated
public class LearnerAssetByCoursewareMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID elementId, UUID changeId, UUID assetId) {
        // @formatter:off
        String QUERY = "INSERT INTO learner.asset_by_courseware ("
                + "  element_id"
                + ", change_id"
                + ", asset_id"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(elementId, changeId, assetId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
