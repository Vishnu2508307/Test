package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DeploymentByActivityMutator extends SimpleTableMutator<DeployedActivity> {

    @Override
    public String getUpsertQuery(DeployedActivity mutation) {
        return "INSERT INTO learner.deployment_by_activity (" +
                "activity_id, " +
                "deployment_id, " +
                "change_id) " +
                "VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeployedActivity mutation) {
        stmt.bind(mutation.getActivityId(), mutation.getId(), mutation.getChangeId());
    }
}
