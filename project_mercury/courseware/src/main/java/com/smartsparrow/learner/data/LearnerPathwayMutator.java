package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerPathwayMutator extends SimpleTableMutator<LearnerPathway> {

    @Override
    public String getUpsertQuery(LearnerPathway mutation) {
        return "INSERT INTO learner.pathway (" +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "config, " +
                "type, " +
                "preload_pathway) VALUES (?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerPathway mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getConfig(),
                mutation.getType().name(),
                mutation.getPreloadPathway().name()
        );
    }
}
