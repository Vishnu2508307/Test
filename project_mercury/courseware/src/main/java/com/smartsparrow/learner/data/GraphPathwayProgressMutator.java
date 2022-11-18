package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.progress.GraphPathwayProgress;
import com.smartsparrow.util.Enums;

public class GraphPathwayProgressMutator extends SimpleTableMutator<GraphPathwayProgress> {

    @Override
    public String getUpsertQuery(GraphPathwayProgress mutation) {
        return "INSERT INTO learner.progress_pathway_graph (" +
                " id" +
                ", deployment_id" +
                ", change_id" +
                ", courseware_element_id" +
                ", courseware_element_type" +
                ", student_id" +
                ", attempt_id" +
                ", evaluation_id" +
                ", completion_value" +
                ", completion_confidence" +
                ", child_completion_values" +
                ", child_completion_confidences" +
                ", child_completed" +
                ", current_walkable_id" +
                ", current_walkable_type" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void bindUpsert(BoundStatement stmt, GraphPathwayProgress mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getDeploymentId());
        stmt.setUUID(2, mutation.getChangeId());
        stmt.setUUID(3, mutation.getCoursewareElementId());
        stmt.setString(4, Enums.asString(mutation.getCoursewareElementType()));
        stmt.setUUID(5, mutation.getStudentId());
        stmt.setUUID(6, mutation.getAttemptId());
        stmt.setUUID(7, mutation.getEvaluationId());

        optionalBind(stmt, 8, mutation.getCompletion().getValue(), Float.class);
        optionalBind(stmt, 9, mutation.getCompletion().getConfidence(), Float.class);

        stmt.setMap(10, mutation.getChildWalkableCompletionValues());
        stmt.setMap(11, mutation.getChildWalkableCompletionConfidences());
        stmt.setList(12, mutation.getCompletedWalkables());
        stmt.setUUID(13, mutation.getCurrentWalkableId());
        stmt.setString(14, Enums.asString(mutation.getCurrentWalkableType()));

    }
}
