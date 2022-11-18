package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class CompletedElementMutator extends SimpleTableMutator<CompletedWalkable> {

    @Override
    public String getUpsertQuery(CompletedWalkable mutation) {
        return "INSERT INTO learner.completed_element (" +
                "deployment_id" +
                ", change_id" +
                ", student_id" +
                ", element_id" +
                ", element_attempt_id" +
                ", evaluation_id" +
                ", parent_element_id" +
                ", parent_element_attempt_id" +
                ", parent_element_type" +
                ", element_type" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CompletedWalkable mutation) {
        stmt.bind(
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getStudentId(),
                mutation.getElementId(),
                mutation.getElementAttemptId(),
                mutation.getEvaluationId(),
                mutation.getParentElementId(),
                mutation.getParentElementAttemptId(),
                Enums.asString(mutation.getParentElementType()),
                Enums.asString(mutation.getElementType())
        );
    }
}
