package com.smartsparrow.learner.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;
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
import com.smartsparrow.learner.progress.RandomPathwayProgress;
import com.smartsparrow.util.Enums;

public class RandomPathwayProgressByCoursewareMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public RandomPathwayProgressByCoursewareMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    // @formatter:off
    private static final String SELECT = "SELECT" +
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
            " FROM learner.progress_pathway_random_by_courseware" +
            " WHERE deployment_id = ?" +
            " AND courseware_element_id = ?" +
            " AND student_id = ?";
    // @formatter:on

    public Statement find(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        return query(SELECT, deploymentId, coursewareElementId, studentId);
    }

    public Statement findLatest(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        //
        String LIMITED = SELECT + " LIMIT 1";
        return query(LIMITED, deploymentId, coursewareElementId, studentId);
    }

    @SuppressWarnings("Duplicates")
    private Statement query(final String query,
                            final UUID deploymentId,
                            final UUID coursewareElementId,
                            final UUID studentId) {
        BoundStatement stmt = stmtCache.asBoundStatement(query);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, coursewareElementId, studentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public RandomPathwayProgress fromRow(final Row row) {
        RandomPathwayProgress progress = new RandomPathwayProgress() //
                .setId(row.getUUID("id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setCoursewareElementId(row.getUUID("courseware_element_id"))
                .setCoursewareElementType(Enums.of(CoursewareElementType.class, row.getString("courseware_element_type")))
                .setStudentId(row.getUUID("student_id"))
                .setAttemptId(row.getUUID("attempt_id"))
                .setEvaluationId(row.getUUID("evaluation_id"))
                .setChildWalkableCompletionValues(row.getMap("child_completion_values", UUID.class, Float.class))
                .setChildWalkableCompletionConfidences(row.getMap("child_completion_confidences", UUID.class, Float.class))
                .setCompletedWalkables(row.getList("child_completed", UUID.class))
                .setInProgressElementId(row.getUUID("in_progress_element_id"))
                .setInProgressElementType(getNullableEnum(row, "in_progress_element_type", CoursewareElementType.class));

        Completion completion = new Completion()
                .setValue(getNullableFloat(row, "completion_value"))
                .setConfidence(getNullableFloat(row, "completion_confidence"));

        if (completion.getValue() != null || completion.getConfidence() != null) {
            progress.setCompletion(completion);
        }

        return progress;
    }
}
