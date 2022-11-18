package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DeploymentMetadataMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DeploymentMetadataMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findCountById(UUID deploymentId, UUID changeId) {

        final String COUNT_QUERY = "select count(*) from learner.metadata_by_deployment where " +
                "deployment_id = ?" +
                "AND change_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(COUNT_QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(deploymentId, changeId);
        return stmt;
    }

    public Long fromRow(Row row) {
        return row.getLong("count");
    }
}
