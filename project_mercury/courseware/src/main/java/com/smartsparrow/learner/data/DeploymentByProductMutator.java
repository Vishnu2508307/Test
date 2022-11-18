package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

import java.util.UUID;

class DeploymentByProductMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(String productId, UUID deploymentId) {
        String UPSERT = "INSERT INTO learner.deployment_by_product (" +
                "product_id, " +
                "deployment_id) " +
                "VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(productId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement remove(String productId, UUID deploymentId) {
        String REMOVE = "DELETE FROM learner.deployment_by_product " +
                "WHERE product_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(productId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
