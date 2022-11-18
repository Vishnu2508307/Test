package com.smartsparrow.learner.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableFloat;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.util.Enums;

class ActivityProgressMateralizer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    ActivityProgressMateralizer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement find(UUID id) {
        // @formatter:off
        final String QUERY = "SELECT "
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
            + " FROM learner.progress_activity"
            + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public ActivityProgress fromRow(final Row row) {
        ActivityProgress progress = new ActivityProgress() //
                .setId(row.getUUID("id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setCoursewareElementId(row.getUUID("courseware_element_id"))
                .setCoursewareElementType(Enums.of(CoursewareElementType.class, //
                                                   row.getString("courseware_element_type")))
                .setStudentId(row.getUUID("student_id"))
                .setAttemptId(row.getUUID("attempt_id"))
                .setEvaluationId(row.getUUID("evaluation_id"))
                //
                .setChildWalkableCompletionValues(row.getMap("child_completion_values", UUID.class, Float.class))
                .setChildWalkableCompletionConfidences(
                        row.getMap("child_completion_confidences", UUID.class, Float.class));

        Completion completion = new Completion()
                .setValue(getNullableFloat(row, "completion_value"))
                .setConfidence(getNullableFloat(row, "completion_confidence"));

        if (completion.getValue() != null || completion.getConfidence() != null) {
            progress.setCompletion(completion);
        }

        return progress;
    }
}

