package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.util.Enums;

public class BKTPathwayProgressMutator extends SimpleTableMutator<BKTPathwayProgress> {

    @Override
    public String getUpsertQuery(final BKTPathwayProgress mutation) {
        // @formatter:off
        return "INSERT INTO learner.progress_pathway_bkt (" +
                " id" +
                ", attempt_id" +
                ", change_id" +
                ", child_completed" +
                ", child_completion_confidences" +
                ", child_completion_values" +
                ", completion_confidence" +
                ", completion_value" +
                ", courseware_element_id" +
                ", courseware_element_type" +
                ", deployment_id" +
                ", evaluation_id" +
                ", in_progress_element_id" +
                ", in_progress_element_type" +
                ", student_id" +
                ", p_ln_minus_given_actual" +
                ", p_ln" +
                ", p_correct" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?," +
                          "?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final BKTPathwayProgress mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setUUID(1, mutation.getAttemptId());
        stmt.setUUID(2, mutation.getChangeId());
        stmt.setList(3, mutation.getCompletedWalkables());
        stmt.setMap(4, mutation.getChildWalkableCompletionConfidences());
        stmt.setMap(5, mutation.getChildWalkableCompletionValues());
        Mutators.bindNonNull(stmt, 6, mutation.getCompletion().getConfidence());
        Mutators.bindNonNull(stmt, 7, mutation.getCompletion().getValue());
        stmt.setUUID(8, mutation.getCoursewareElementId());
        stmt.setString(9, Enums.asString(mutation.getCoursewareElementType()));
        stmt.setUUID(10, mutation.getDeploymentId());
        stmt.setUUID(11, mutation.getEvaluationId());
        Mutators.bindNonNull(stmt, 12, mutation.getInProgressElementId());
        Mutators.bindNonNull(stmt, 13, mutation.getInProgressElementType());
        stmt.setUUID(14, mutation.getStudentId());
        stmt.setDouble(15, mutation.getpLnMinusGivenActual());
        stmt.setDouble(16, mutation.getpLn());
        stmt.setDouble(17, mutation.getpCorrect());
    }
}
