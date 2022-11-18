package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentByLearnerComponentMutator extends SimpleTableMutator<ParentByLearnerComponent> {

    @Override
    public String getUpsertQuery(ParentByLearnerComponent mutation) {
        return "INSERT INTO learner.parent_by_component (" +
                "component_id, " +
                "deployment_id, " +
                "change_id, " +
                "parent_id, " +
                "parent_type) VALUES(?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ParentByLearnerComponent mutation) {
        stmt.bind(
                mutation.getComponentId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getParentId(),
                mutation.getParentType().name()
        );
    }
}
