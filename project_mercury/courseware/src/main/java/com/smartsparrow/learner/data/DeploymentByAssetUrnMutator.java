package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DeploymentByAssetUrnMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(final String assetUrn, final Deployment deployment) {
        final String INSERT = "INSERT INTO learner.deployment_by_asset_urn (" +
                "asset_urn, " +
                "deployment_id, " +
                "change_id) " +
                "VALUES (?, ?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(INSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetUrn, deployment.getId(), deployment.getChangeId());
        stmt.setIdempotent(true);
        return stmt;
    }
}
