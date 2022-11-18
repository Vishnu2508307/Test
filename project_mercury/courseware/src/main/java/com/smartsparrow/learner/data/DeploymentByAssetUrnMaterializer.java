package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DeploymentByAssetUrnMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DeploymentByAssetUrnMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findDeployments(final String assetUrn) {
        final String SELECT = "SELECT " +
                "deployment_id, " +
                "change_id " +
                "FROM learner.deployment_by_asset_urn " +
                "WHERE asset_urn = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetUrn);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Deployment fromRow(Row row) {
        return new Deployment()
                .setChangeId(row.getUUID("change_id"))
                .setId(row.getUUID("deployment_id"));
    }
}
