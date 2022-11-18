package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.attempt.Attempt;

class AttemptByCoursewareMutator extends SimpleTableMutator<Attempt> {

    @Override
    public String getUpsertQuery(Attempt mutation) {
        // @formatter:off
        return "INSERT INTO learner.attempt_by_courseware ("
                + "  id"
                + ", parent_id"
                + ", deployment_id"
                + ", courseware_element_id"
                + ", courseware_element_type"
                + ", student_id"
                + ", value"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Attempt mutation) {
        stmt.bind(mutation.getId(), //
                  mutation.getParentId(), //
                  mutation.getDeploymentId(), //
                  mutation.getCoursewareElementId(), //
                  mutation.getCoursewareElementType().name(), //
                  mutation.getStudentId(), //
                  mutation.getValue());
    }
}
