package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DeploymentMetadataMutator extends SimpleTableMutator<LearnerCoursewareElement> {

    @Override
    public String getUpsertQuery(LearnerCoursewareElement mutation) {
        return "INSERT INTO learner.metadata_by_deployment (" +
                "deployment_id, " +
                "change_id, " +
                "element_id) VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerCoursewareElement mutation) {
        stmt.bind(
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getId()
        );
    }
}
