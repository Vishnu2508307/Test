package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class LearnerManualGradingComponentByWalkableMutator extends SimpleTableMutator<LearnerManualGradingComponentByWalkable> {

    @Override
    public String getUpsertQuery(LearnerManualGradingComponentByWalkable mutation) {
        return "INSERT INTO learner.manual_grading_component_by_walkable (" +
                " deployment_id" +
                ", walkable_id" +
                ", component_id" +
                ", change_id" +
                ", walkable_type" +
                ", component_parent_id" +
                ", component_parent_type" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerManualGradingComponentByWalkable mutation) {
        stmt.bind(
                mutation.getDeploymentId(),
                mutation.getWalkableId(),
                mutation.getComponentId(),
                mutation.getChangeId(),
                Enums.asString(mutation.getWalkableType()),
                mutation.getComponentParentId(),
                Enums.asString(mutation.getComponentParentType())
        );
    }
}
