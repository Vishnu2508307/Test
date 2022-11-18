package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

import java.util.UUID;

class CohortByDeploymentMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID cohortId, UUID deploymentId) {
        String UPSERT = "INSERT INTO learner.cohort_by_deployment (" +
                "deployment_id, " +
                "cohort_id) " +
                "VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement remove(UUID cohortId, UUID deploymentId) {
        String REMOVE = "DELETE FROM learner.cohort_by_deployment " +
                "WHERE deployment_id = ? " +
                "AND cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
