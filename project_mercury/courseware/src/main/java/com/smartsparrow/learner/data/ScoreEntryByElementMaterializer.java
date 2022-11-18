package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.util.Enums;

public class ScoreEntryByElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ScoreEntryByElementMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(UUID deploymentId, UUID studentId, UUID elementId, UUID attemptId) {
        final String SELECT = "SELECT" +
                " cohort_id" +
                ", deployment_id" +
                ", change_id" +
                ", student_id" +
                ", element_id" +
                ", attempt_id" +
                ", id" +
                ", value" +
                ", adjustment_value" +
                ", evaluation_id" +
                ", operator" +
                ", source_element_id" +
                ", source_scenario_id" +
                ", source_account_id" +
                ", element_type" +
                " FROM learner.score_entry_by_element" +
                " WHERE deployment_id = ?" +
                " AND student_id = ?" +
                " AND element_id = ?" +
                " AND attempt_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, studentId, elementId, attemptId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public StudentScoreEntry fromRow(Row row) {
        return new StudentScoreEntry()
                .setCohortId(row.getUUID("cohort_id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setStudentId(row.getUUID("student_id"))
                .setElementId(row.getUUID("element_id"))
                .setAttemptId(row.getUUID("attempt_id"))
                .setId(row.getUUID("id"))
                .setValue(row.getDouble("value"))
                .setAdjustmentValue(row.getDouble("adjustment_value"))
                .setEvaluationId(row.getUUID("evaluation_id"))
                .setOperator(Enums.of(MutationOperator.class, row.getString("operator")))
                .setSourceElementId(row.getUUID("source_element_id"))
                .setSourceScenarioId(row.getUUID("source_scenario_id"))
                .setSourceAccountId(row.getUUID("source_account_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")));

    }
}
