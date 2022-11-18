package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class EvaluationResultByAttemptMutator extends SimpleTableMutator<Evaluation> {

    @Override
    public String getUpsertQuery(final Evaluation mutation) {
        // @formatter:off
        return "INSERT INTO learner.evaluation_by_attempt_id ("
                + "  id"
                + ", courseware_element_id"
                + ", courseware_change_id"
                + ", deployment_id"
                + ", attempt_id"
                + ", interactive_complete"
                + ", cohort_id"
                + ") VALUES ( ?, ?, ?, ?, ?, ? ,?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final Evaluation mutation) {
        stmt.bind(mutation.getId(), //
                mutation.getElementId(), //
                mutation.getDeployment().getChangeId(), //
                mutation.getDeployment().getId(), //
                mutation.getAttemptId(), //
                mutation.getCompleted(),
                mutation.getDeployment().getCohortId());
    }

}
