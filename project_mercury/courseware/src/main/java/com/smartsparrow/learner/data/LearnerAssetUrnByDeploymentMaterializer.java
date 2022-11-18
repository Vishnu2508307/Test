package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LearnerAssetUrnByDeploymentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerAssetUrnByDeploymentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAssets(Deployment deployment) {
        final String QUERY = "SELECT " +
                "asset_urn " +
                "FROM learner.asset_urn_by_deployment " +
                "WHERE deployment_id = ? " +
                "AND change_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deployment.getId(), deployment.getChangeId());
        stmt.setIdempotent(true);
        return stmt;
    }

    public String fromRow(final Row row) {
        return row.getString("asset_urn");
    }
}
