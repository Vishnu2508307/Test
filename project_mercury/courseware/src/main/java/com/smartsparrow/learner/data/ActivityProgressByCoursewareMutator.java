package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.progress.ActivityProgress;

class ActivityProgressByCoursewareMutator extends SimpleTableMutator<ActivityProgress> {

    @Override
    public String getUpsertQuery(ActivityProgress mutation) {
        // @formatter:off
        return "INSERT INTO learner.progress_activity_by_courseware ("
                + "  id"
                + ", deployment_id"
                + ", change_id"
                + ", courseware_element_id"
                + ", courseware_element_type"
                + ", student_id"
                + ", attempt_id"
                + ", evaluation_id"
                + ", completion_value"
                + ", completion_confidence"
                + ", child_completion_values"
                + ", child_completion_confidences"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void bindUpsert(BoundStatement stmt, ActivityProgress mutation) {
        stmt.bind(mutation.getId(), //
                  mutation.getDeploymentId(), //
                  mutation.getChangeId(), //
                  mutation.getCoursewareElementId(),//
                  mutation.getCoursewareElementType().name(),//
                  mutation.getStudentId(),//
                  mutation.getAttemptId(),//
                  mutation.getEvaluationId(),//
                  mutation.getCompletion() == null ? null : mutation.getCompletion().getValue(),//
                  mutation.getCompletion() == null ? null : mutation.getCompletion().getConfidence(),//
                  mutation.getChildWalkableCompletionValues(), //
                  mutation.getChildWalkableCompletionConfidences());
    }
}
