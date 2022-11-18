package com.smartsparrow.annotation.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.annotation.service.DeploymentAnnotation;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class DeploymentAnnotationMutator extends SimpleTableMutator<DeploymentAnnotation> {

    @Override
    public String getUpsertQuery(DeploymentAnnotation mutation) {
        return "INSERT INTO learner.deployment_annotation (" +
                "deployment_id" +
                ", change_id" +
                ", motivation" +
                ", element_id" +
                ", annotation_id" +
                ", creator_account_id" +
                ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeploymentAnnotation mutation) {
        stmt.bind(
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                Enums.asString(mutation.getMotivation()),
                mutation.getElementId(),
                mutation.getId(),
                mutation.getCreatorAccountId()
        );
    }
}
