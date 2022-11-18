package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerElementMetaInformationMutator extends SimpleTableMutator<LearnerElementMetaInformation> {

    @Override
    public String getUpsertQuery(final LearnerElementMetaInformation mutation) {
        return "INSERT INTO learner.learner_element_meta_information (" +
                " element_id" +
                ", deployment_id" +
                ", change_id" +
                ", key" +
                ", value" +
                ") VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final LearnerElementMetaInformation mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getKey(),
                mutation.getValue()
        );
    }
}
