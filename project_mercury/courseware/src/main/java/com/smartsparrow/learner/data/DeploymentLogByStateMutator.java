package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class DeploymentLogByStateMutator extends SimpleTableMutator<DeploymentStepLog> {

    @Override
    public String getUpsertQuery(DeploymentStepLog mutation) {
        return "INSERT INTO learner.deployment_log_by_state (" +
                " cohort_id" +
                ", deployment_id" +
                ", change_id" +
                ", state" +
                ", id" +
                ", message" +
                ", element_id" +
                ", element_type)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeploymentStepLog mutation) {
        stmt.bind(
                mutation.getDeployment().getCohortId(),
                mutation.getDeployment().getId(),
                mutation.getDeployment().getChangeId(),
                Enums.asString(mutation.getState()),
                mutation.getId(),
                mutation.getMessage(),
                mutation.getElement().getElementId(),
                Enums.asString(mutation.getElement().getElementType())
        );
    }
}
