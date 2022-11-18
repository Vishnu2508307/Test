package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.progress.FreePathwayProgress;

class FreePathwayProgressByCoursewareMutator extends SimpleTableMutator<FreePathwayProgress> {

    @Override
    public String getUpsertQuery(FreePathwayProgress mutation) {
        // @formatter:off
        return "INSERT INTO learner.progress_pathway_free_by_courseware ("
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
                + ", child_completed"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void bindUpsert(BoundStatement stmt, FreePathwayProgress mutation) {
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
                  mutation.getChildWalkableCompletionConfidences(), //
                  mutation.getCompletedWalkables());
    }
}
