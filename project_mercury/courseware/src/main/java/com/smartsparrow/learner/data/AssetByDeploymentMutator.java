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
public class AssetByDeploymentMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID assetId, Deployment deployment) {
        final String UPSERT = "INSERT INTO learner.asset_by_deployment (" +
                "deployment_id, " +
                "change_id, " +
                "asset_id) " +
                "VALUES (?,?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deployment.getId(), deployment.getChangeId(), assetId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
