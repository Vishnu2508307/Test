package com.smartsparrow.annotation.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class LearnerAnnotationByMotivationMutator extends SimpleTableMutator<LearnerAnnotation> {

    @Override
    public String getUpsertQuery(LearnerAnnotation mutation) {
        // @formatter:off
        return "INSERT INTO learner.annotation_by_motivation ("
                + "  deployment_id"
                + ", creator_account_id"
                + ", motivation"
                + ", element_id"
                + ", annotation_id"
                + ") VALUES ( ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerAnnotation mutation) {
        stmt.bind(mutation.getDeploymentId(), //
                  mutation.getCreatorAccountId(), //
                  Enums.asString(mutation.getMotivation()), //
                  mutation.getElementId(), //
                  mutation.getId());
    }

    @Override
    public String getDeleteQuery(LearnerAnnotation mutation) {
        // @formatter:off
        return "DELETE FROM learner.annotation_by_motivation"
                + " WHERE deployment_id = ?"
                + "   AND creator_account_id = ?"
                + "   AND motivation = ?"
                + "   AND element_id = ?"
                + "   AND annotation_id = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, LearnerAnnotation mutation) {
        stmt.bind(mutation.getDeploymentId(), //
                  mutation.getCreatorAccountId(), //
                  Enums.asString(mutation.getMotivation()), //
                  mutation.getElementId(), //
                  mutation.getId());
    }

}
