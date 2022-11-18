package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DeploymentMutator extends SimpleTableMutator<DeployedActivity> {

    @Override
    public String getUpsertQuery(DeployedActivity mutation) {
        return "INSERT INTO learner.deployment (" +
                "id, " +
                "activity_id, " +
                "change_id," +
                "cohort_id) " +
                "VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeployedActivity mutation) {
        stmt.bind(mutation.getId(), mutation.getActivityId(), mutation.getChangeId(), mutation.getCohortId());
    }
}
