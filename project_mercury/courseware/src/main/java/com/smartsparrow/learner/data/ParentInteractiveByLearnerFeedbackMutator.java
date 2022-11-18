package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentInteractiveByLearnerFeedbackMutator extends SimpleTableMutator<LearnerParentElement> {

    @Override
    public String getUpsertQuery(LearnerParentElement mutation) {
        return "INSERT INTO learner.parent_interactive_by_feedback (" +
                "feedback_id, " +
                "deployment_id, " +
                "change_id, " +
                "interactive_id) VALUES(?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerParentElement mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getParentId()
        );
    }
}
