package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

/**
 * This class has been deprecated as the completed walkable is stored with change_id being part of the primary key.
 * That is not an ideal behaviour since completed walkables will not be displayed if the lesson is re-published.
 */
@Deprecated
public class DeploymentByAssetMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(Deployment deployment, UUID assetId) {
        final String INSERT = "INSERT INTO learner.deployment_by_asset (" +
                "asset_id, " +
                "deployment_id, " +
                "change_id) " +
                "VALUES (?,?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(INSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId, deployment.getId(), deployment.getChangeId());
        stmt.setIdempotent(true);
        return stmt;
    }
}
