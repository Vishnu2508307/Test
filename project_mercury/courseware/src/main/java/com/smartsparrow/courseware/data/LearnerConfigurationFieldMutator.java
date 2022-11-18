package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerConfigurationFieldMutator extends SimpleTableMutator<LearnerElementConfigurationField> {

    @Override
    public String getUpsertQuery(LearnerElementConfigurationField mutation) {
        return "INSERT INTO learner.configuration_field_by_element (" +
                " deployment_id" +
                ", change_id" +
                ", element_id" +
                ", field_name" +
                ", field_value)" +
                " VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerElementConfigurationField mutation) {
        stmt.bind(
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getElementId(),
                mutation.getFieldName(),
                mutation.getFieldValue()
        );
    }
}
