package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.util.Enums;

public class BKTPathwayProgressByCoursewareMutator extends SimpleTableMutator<BKTPathwayProgress> {

    @Override
    public String getUpsertQuery(final BKTPathwayProgress mutation) {
        return "INSERT INTO learner.progress_pathway_bkt_by_courseware (" +
                " deployment_id" +
                ", courseware_element_id" +
                ", student_id" +
                ", id" +
                ", attempt_id" +
                ", change_id" +
                ", child_completed" +
                ", child_completion_confidences" +
                ", child_completion_values" +
                ", completion_confidence" +
                ", completion_value" +
                ", courseware_element_type" +
                ", evaluation_id" +
                ", in_progress_element_id" +
                ", in_progress_element_type" +
                ", p_ln_minus_given_actual" +
                ", p_ln" +
                ", p_correct" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?," +
                          "?, ?, ? ,? ,? ,? ,? ,?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final BKTPathwayProgress mutation) {
        stmt.setUUID(0, mutation.getDeploymentId());
        stmt.setUUID(1, mutation.getCoursewareElementId());
        stmt.setUUID(2, mutation.getStudentId());
        stmt.setUUID(3, mutation.getId());
        stmt.setUUID(4, mutation.getAttemptId());
        stmt.setUUID(5, mutation.getChangeId());
        stmt.setList(6, mutation.getCompletedWalkables());
        stmt.setMap(7, mutation.getChildWalkableCompletionConfidences());
        stmt.setMap(8, mutation.getChildWalkableCompletionValues());
        Mutators.bindNonNull(stmt, 9, mutation.getCompletion().getConfidence());
        Mutators.bindNonNull(stmt, 10, mutation.getCompletion().getValue());
        stmt.setString(11, Enums.asString(mutation.getCoursewareElementType()));
        stmt.setUUID(12, mutation.getEvaluationId());
        Mutators.bindNonNull(stmt, 13, mutation.getInProgressElementId());
        Mutators.bindNonNull(stmt, 14, mutation.getInProgressElementType());
        stmt.setDouble(15, mutation.getpLnMinusGivenActual());
        stmt.setDouble(16, mutation.getpLn());
        stmt.setDouble(17, mutation.getpCorrect());
    }
}
