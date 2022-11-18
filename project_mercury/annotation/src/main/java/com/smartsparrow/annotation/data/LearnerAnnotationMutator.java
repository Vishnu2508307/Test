package com.smartsparrow.annotation.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class LearnerAnnotationMutator extends SimpleTableMutator<LearnerAnnotation> {

    @Override
    public String getUpsertQuery(LearnerAnnotation mutation) {
        // @formatter:off
        return "INSERT INTO learner.annotation ("
                + "  id"
                + ", version"
                + ", annotation_type"
                + ", motivation"
                + ", creator_account_id"
                + ", body"
                + ", target"
                + ", deployment_id"
                + ", element_id"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerAnnotation mutation) {
        stmt.bind(mutation.getId(), //
                  mutation.getVersion(), //
                  Enums.asString(mutation.getType()), //
                  Enums.asString(mutation.getMotivation()), //
                  mutation.getCreatorAccountId(), //
                  mutation.getBody(), //
                  mutation.getTarget(), //
                  mutation.getDeploymentId(), //
                  mutation.getElementId());
    }

    @Override
    public String getDeleteQuery(LearnerAnnotation mutation) {
        // @formatter:off
        return "DELETE FROM learner.annotation"
                + " WHERE id = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, LearnerAnnotation mutation) {
        stmt.bind(mutation.getId());
    }

}
