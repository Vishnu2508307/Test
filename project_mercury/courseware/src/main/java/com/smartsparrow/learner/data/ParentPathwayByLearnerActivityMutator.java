package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentPathwayByLearnerActivityMutator extends SimpleTableMutator<LearnerParentElement> {

    @Override
    public String getUpsertQuery(LearnerParentElement mutation) {
        return "INSERT INTO learner.parent_pathway_by_activity (" +
                "activity_id, " +
                "deployment_id, " +
                "change_id, " +
                "pathway_id) VALUES (?,?,?,?)";
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
