package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class DeploymentLogByElementMutator extends SimpleTableMutator<DeploymentStepLog> {

    @Override
    public String getUpsertQuery(DeploymentStepLog mutation) {
        return "INSERT INTO learner.deployment_log_by_element (" +
                " cohort_id" +
                ", deployment_id" +
                ", change_id" +
                ", element_id" +
                ", id" +
                ", state" +
                ", message" +
                ", element_type)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeploymentStepLog mutation) {
        stmt.bind(
                mutation.getDeployment().getCohortId(),
                mutation.getDeployment().getId(),
                mutation.getDeployment().getChangeId(),
                mutation.getElement().getElementId(),
                mutation.getId(),
                Enums.asString(mutation.getState()),
                mutation.getMessage(),
                Enums.asString(mutation.getElement().getElementType())
        );
    }
}
