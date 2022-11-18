package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerScenarioMutator extends SimpleTableMutator<LearnerScenario> {

    @Override
    public String getUpsertQuery(LearnerScenario mutation) {
        return "INSERT INTO learner.scenario (" +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "actions, " +
                "condition, " +
                "correctness, " +
                "lifecycle, " +
                "name, " +
                "description) VALUES(?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerScenario mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getActions(),
                mutation.getCondition(),
                (mutation.getCorrectness() != null ? mutation.getCorrectness().name() : null),
                mutation.getLifecycle().name(),
                mutation.getName(),
                mutation.getDescription()
        );
    }
}
