package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ScoreEntryByElementMutator extends SimpleTableMutator<StudentScoreEntry> {

    @Override
    public String getUpsertQuery(StudentScoreEntry mutation) {
        return "INSERT INTO learner.score_entry_by_element (" +
                " cohort_id" +
                ", deployment_id" +
                ", change_id" +
                ", student_id" +
                ", element_id" +
                ", attempt_id" +
                ", id" +
                ", value" +
                ", adjustment_value" +
                ", operator" +
                ", element_type" +
                ", evaluation_id" +
                ", source_element_id" +
                ", source_scenario_id" +
                ", source_account_id" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, StudentScoreEntry mutation) {
        stmt.setUUID(0, mutation.getCohortId())
                .setUUID(1, mutation.getDeploymentId())
                .setUUID(2, mutation.getChangeId())
                .setUUID(3, mutation.getStudentId())
                .setUUID(4, mutation.getElementId())
                .setUUID(5, mutation.getAttemptId())
                .setUUID(6, mutation.getId())
                .setDouble(7, mutation.getValue())
                .setDouble(8, mutation.getAdjustmentValue())
                .setString(9, Enums.asString(mutation.getOperator()))
                .setString(10, Enums.asString(mutation.getElementType()));

        // optional bind the next field since they can be null
        optionalBind(stmt, 11, mutation.getEvaluationId(), UUID.class);
        optionalBind(stmt, 12, mutation.getSourceElementId(), UUID.class);
        optionalBind(stmt, 13, mutation.getSourceScenarioId(), UUID.class);
        optionalBind(stmt, 14, mutation.getSourceAccountId(), UUID.class);
    }
}
