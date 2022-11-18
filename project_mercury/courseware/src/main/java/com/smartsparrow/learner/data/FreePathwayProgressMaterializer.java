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
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.FreePathwayProgress;
import com.smartsparrow.util.Enums;

class FreePathwayProgressMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    FreePathwayProgressMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

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
            + ", child_completed"
            + " FROM learner.progress_pathway_free"
            + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public FreePathwayProgress fromRow(final Row row) {
        FreePathwayProgress progress = new FreePathwayProgress() //
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
                        row.getMap("child_completion_confidences", UUID.class, Float.class))
                .setCompletedWalkables(row.getList("child_completed", UUID.class));

        Completion completion = new Completion()
                .setValue(getNullableFloat(row, "completion_value"))
                .setConfidence(getNullableFloat(row, "completion_confidence"));

        if (completion.getValue() != null || completion.getConfidence() != null) {
            progress.setCompletion(completion);
        }

        return progress;
    }
}
