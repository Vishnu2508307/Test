package com.smartsparrow.courseware.data;

import static com.smartsparrow.dse.api.Mutators.bindNonNull;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ManualGradingConfigurationMutator extends SimpleTableMutator<ManualGradingConfiguration> {

    @Override
    public String getUpsertQuery(ManualGradingConfiguration mutation) {
        return "INSERT INTO courseware.manual_grading_configuration_by_component (" +
                " component_id" +
                ", max_score" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ManualGradingConfiguration mutation) {
        stmt.setUUID(0, mutation.getComponentId());
        bindNonNull(stmt, 1, mutation.getMaxScore(), Double.class);
    }

    @Override
    public String getDeleteQuery(ManualGradingConfiguration mutation) {
        return "DELETE FROM courseware.manual_grading_configuration_by_component" +
                " WHERE component_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ManualGradingConfiguration mutation) {
        stmt.bind(mutation.getComponentId());
    }
}
