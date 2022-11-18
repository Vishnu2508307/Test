package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentActivityByLearnerPathwayMutator extends SimpleTableMutator<LearnerParentElement> {

    @Override
    public String getUpsertQuery(LearnerParentElement mutation) {
        return "INSERT INTO learner.parent_activity_by_pathway (" +
                "pathway_id, " +
                "deployment_id, " +
                "change_id, " +
                "activity_id) VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerParentElement mutation) {
        stmt.bind(
                mutation.getElementId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getParentId()
        );
    }
}
