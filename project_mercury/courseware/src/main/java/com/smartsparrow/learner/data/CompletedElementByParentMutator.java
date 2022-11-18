package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CompletedElementByParentMutator extends SimpleTableMutator<CompletedWalkable> {

    @Override
    public String getUpsertQuery(CompletedWalkable mutation) {
        // formatter:off
        return "INSERT INTO learner.completed_element_by_parent (" +
                "  deployment_id" +
                ", change_id" +
                ", student_id" +
                ", parent_element_id" +
                ", parent_element_attempt_id" +
                ", evaluation_id" +
                ", element_id" +
                ", element_attempt_id" +
                ", parent_element_type" +
                ", element_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CompletedWalkable mutation) {
        stmt.bind(
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getStudentId(),
                mutation.getParentElementId(),
                mutation.getParentElementAttemptId(),
                mutation.getEvaluationId(),
                mutation.getElementId(),
                mutation.getElementAttemptId(),
                mutation.getParentElementType().name(),
                mutation.getElementType().name()
        );
    }
}
