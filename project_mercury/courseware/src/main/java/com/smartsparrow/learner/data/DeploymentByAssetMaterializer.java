package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

/**
 * This class has been deprecated as the completed walkable is stored with change_id being part of the primary key.
 * That is not an ideal behaviour since completed walkables will not be displayed if the lesson is re-published.
 */
@Deprecated
public class DeploymentByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DeploymentByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findDeployments(UUID assetId) {
        final String SELECT = "SELECT " +
                "deployment_id, " +
                "change_id " +
                "FROM learner.deployment_by_asset " +
                "WHERE asset_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Deployment fromRow(Row row) {
        return new Deployment()
                .setChangeId(row.getUUID("change_id"))
                .setId(row.getUUID("deployment_id"));
    }
}
