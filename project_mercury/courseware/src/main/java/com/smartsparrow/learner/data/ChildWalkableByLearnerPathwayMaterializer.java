package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ChildWalkableByLearnerPathwayMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ChildWalkableByLearnerPathwayMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatestByDeployment(UUID pathwayId, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "pathway_id, " +
                "deployment_id, " +
                "change_id, " +
                "walkable_ids, " +
                "walkable_types " +
                "FROM learner.child_walkable_by_pathway " +
                "WHERE pathway_id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pathwayId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerWalkablePathwayChildren fromRow(Row row) {
        return new LearnerWalkablePathwayChildren()
                .setPathwayId(row.getUUID("pathway_id"))
                .setWalkableIds(row.getList("walkable_ids", UUID.class))
                .setWalkableTypes(row.getMap("walkable_types", UUID.class, String.class))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"));
    }
}
