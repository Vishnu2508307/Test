package com.smartsparrow.courseware.data;

import static com.smartsparrow.dse.api.Mutators.bindNonNull;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class LearnerManualGradingConfigurationMutator extends SimpleTableMutator<LearnerManualGradingConfiguration> {

    @Override
    public String getUpsertQuery(LearnerManualGradingConfiguration mutation) {
        return "INSERT INTO learner.manual_grading_configuration_by_deployment (" +
                " deployment_id" +
                ", component_id" +
                ", max_score" +
                ", change_id" +
                ", parent_id" +
                ", parent_type" +
                ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerManualGradingConfiguration mutation) {
        stmt.setUUID(0, mutation.getDeploymentId());
        stmt.setUUID(1, mutation.getComponentId());
        bindNonNull(stmt, 2, mutation.getMaxScore(), Double.class);
        stmt.setUUID(3, mutation.getChangeId());
        stmt.setUUID(4, mutation.getParentId());
        stmt.setString(5, Enums.asString(mutation.getParentType()));
    }
}
