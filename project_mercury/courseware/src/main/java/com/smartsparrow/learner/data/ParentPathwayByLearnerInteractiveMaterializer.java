package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ParentPathwayByLearnerInteractiveMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ParentPathwayByLearnerInteractiveMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatestDeployed(UUID interactiveId, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "pathway_id " +
                "FROM learner.parent_pathway_by_interactive " +
                "WHERE interactive_id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(interactiveId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("pathway_id");
    }
}
