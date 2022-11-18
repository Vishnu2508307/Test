package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class EvaluationResultByAttemptMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    EvaluationResultByAttemptMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByAttempt(final UUID attemptId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", courseware_element_id"
                + ", courseware_change_id"
                + ", deployment_id"
                + ", attempt_id"
                + ", interactive_complete"
                + ", cohort_id"
                + " FROM learner.evaluation_by_attempt_id"
                + " WHERE attempt_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(attemptId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public EvaluationResult fromRow(final Row row) {
        final Deployment deployment = new Deployment()
                .setId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("courseware_change_id"))
                .setCohortId(row.getUUID("cohort_id"));

        return new EvaluationResult() //
                .setId(row.getUUID("id"))
                .setCoursewareElementId(row.getUUID("courseware_element_id"))
                .setDeployment(deployment)
                .setAttemptId(row.getUUID("attempt_id"))
                .setInteractiveComplete(row.getBool("interactive_complete"));
    }
}
