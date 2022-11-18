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

class LearnerAnnotationByMotivationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    LearnerAnnotationByMotivationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetch(UUID deploymentId, UUID creatorAccountId, Motivation motivation) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  annotation_id"
                + " FROM learner.annotation_by_motivation"
                + " WHERE deployment_id=?"
                + "   AND creator_account_id=?"
                + "   AND motivation=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, creatorAccountId, Enums.asString(motivation));
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetch(UUID deploymentId, UUID creatorAccountId, Motivation motivation, UUID elementId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  annotation_id"
                + " FROM learner.annotation_by_motivation"
                + " WHERE deployment_id=?"
                + "   AND creator_account_id=?"
                + "   AND motivation=?"
                + "   AND element_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, creatorAccountId, Enums.asString(motivation), elementId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("annotation_id");
    }
}
