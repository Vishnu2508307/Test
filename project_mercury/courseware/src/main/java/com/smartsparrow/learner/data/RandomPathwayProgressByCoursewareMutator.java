package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.progress.RandomPathwayProgress;
import com.smartsparrow.util.Enums;

public class RandomPathwayProgressByCoursewareMutator extends SimpleTableMutator<RandomPathwayProgress> {

    @Override
    public String getUpsertQuery(final RandomPathwayProgress mutation) {
        return "INSERT INTO learner.progress_pathway_random_by_courseware (" +
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
                ", in_progress_element_id" +
                ", in_progress_element_type" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void bindUpsert(final BoundStatement stmt, final RandomPathwayProgress mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getDeploymentId());
        stmt.setUUID(2, mutation.getChangeId());
        stmt.setUUID(3, mutation.getCoursewareElementId());
        stmt.setString(4, Enums.asString(mutation.getCoursewareElementType()));
        stmt.setUUID(5, mutation.getStudentId());
        stmt.setUUID(6, mutation.getAttemptId());
        stmt.setUUID(7, mutation.getEvaluationId());

        Mutators.bindNonNull(stmt, 8, mutation.getCompletion().getValue());
        Mutators.bindNonNull(stmt, 9, mutation.getCompletion().getConfidence());

        stmt.setMap(10, mutation.getChildWalkableCompletionValues());
        stmt.setMap(11, mutation.getChildWalkableCompletionConfidences());
        stmt.setList(12, mutation.getCompletedWalkables());

        Mutators.bindNonNull(stmt, 13, mutation.getInProgressElementId());
        Mutators.bindNonNull(stmt, 14, mutation.getInProgressElementType());
    }
}
