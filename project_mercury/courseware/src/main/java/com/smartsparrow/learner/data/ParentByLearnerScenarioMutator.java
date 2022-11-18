package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentByLearnerScenarioMutator extends SimpleTableMutator<ParentByLearnerScenario> {

    @Override
    public String getUpsertQuery(ParentByLearnerScenario mutation) {
        return "INSERT INTO learner.parent_by_scenario (" +
                "scenario_id, " +
                "deployment_id, " +
                "change_id, " +
                "parent_id, " +
                "parent_type) VALUES(?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ParentByLearnerScenario mutation) {
        stmt.bind(
                mutation.getScenarioId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getParentId(),
                mutation.getParentType().name()
        );
    }
}
