package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerAssetUrnByDeploymentMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(final String assetUrn, final Deployment deployment) {
        final String UPSERT = "INSERT INTO learner.asset_urn_by_deployment (" +
                "deployment_id, " +
                "change_id, " +
                "asset_urn) " +
                "VALUES (?,?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deployment.getId(), deployment.getChangeId(), assetUrn);
        stmt.setIdempotent(true);
        return stmt;
    }
}
