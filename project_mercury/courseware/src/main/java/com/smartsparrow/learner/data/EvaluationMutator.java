package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class EvaluationMutator extends SimpleTableMutator<Evaluation> {

    @Override
    public String getUpsertQuery(Evaluation mutation) {
        // formatter:off
        return "INSERT INTO learner.evaluation (" +
                "  id" +
                ", element_id" +
                ", element_type" +
                ", deployment_id" +
                ", change_id" +
                ", student_id" +
                ", attempt_id" +
                ", element_scope_data_map" +
                ", student_scope_urn" +
                ", parent_id" +
                ", parent_type" +
                ", parent_attempt_id" +
                ", completed" +
                ", triggered_scenario_ids" +
                ", scenario_correctness" +
                ", cohort_id" +
                ", triggered_actions" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Evaluation mutation) {
        String scenarioCorrectness = null;

        if (mutation.getScenarioCorrectness() != null) {
            scenarioCorrectness = Enums.asString(mutation.getScenarioCorrectness());
        }

        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getElementId());
        stmt.setString(2, Enums.asString(mutation.getElementType()));
        stmt.setUUID(3, mutation.getDeployment().getId());
        stmt.setUUID(4, mutation.getDeployment().getChangeId());
        stmt.setUUID(5, mutation.getStudentId());
        stmt.setUUID(6, mutation.getAttemptId());
        stmt.setMap(7, mutation.getElementScopeDataMap());
        stmt.setUUID(8, mutation.getStudentScopeURN());
        stmt.setUUID(9, mutation.getParentId());
        stmt.setString(10, Enums.asString(mutation.getParentType()));
        stmt.setUUID(11, mutation.getParentAttemptId());
        stmt.setBool(12, mutation.getCompleted());
        stmt.setList(13, mutation.getTriggeredScenarioIds());
        optionalBind(stmt, 14, scenarioCorrectness, String.class);
        stmt.setUUID(15, mutation.getDeployment().getCohortId());
        stmt.setString(16, mutation.getTriggeredActions());
    }
}
