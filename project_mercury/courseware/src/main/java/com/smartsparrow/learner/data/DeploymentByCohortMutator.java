package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DeploymentByCohortMutator extends SimpleTableMutator<UUID> {

    public Statement insert(UUID cohortId, UUID deploymentId) {
        String UPSERT = "INSERT INTO learner.deployment_by_cohort (" +
                "cohort_id, " +
                "deployment_id) " +
                "VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement remove(UUID cohortId, UUID deploymentId) {
        String REMOVE = "DELETE FROM learner.deployment_by_cohort " +
                "WHERE cohort_id = ? " +
                "AND deployment_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
