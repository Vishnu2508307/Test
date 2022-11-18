package com.smartsparrow.annotation.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class DeploymentAnnotationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DeploymentAnnotationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetch(final UUID deploymentId, final UUID changeId, final Motivation motivation) {
        final String QUERY = "SELECT" +
                " annotation_id" +
                " FROM learner.deployment_annotation" +
                " WHERE deployment_id = ?" +
                " AND change_id = ?" +
                " AND motivation = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, changeId, Enums.asString(motivation));
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetch(final UUID deploymentId, final UUID changeId, final Motivation motivation,
                           final UUID elementId) {
        final String QUERY = "SELECT" +
                " annotation_id" +
                " FROM learner.deployment_annotation" +
                " WHERE deployment_id = ?" +
                " AND change_id = ?" +
                " AND motivation = ?" +
                " AND element_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, changeId, Enums.asString(motivation), elementId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("annotation_id");
    }

}
