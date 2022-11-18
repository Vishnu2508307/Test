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
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.util.Enums;

public class BKTPathwayProgressByCoursewareMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public BKTPathwayProgressByCoursewareMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatestN(UUID deploymentId, UUID coursewareElementId, UUID studentId, int limit) {
        final String SELECT = "SELECT" +
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
                " FROM learner.progress_pathway_bkt_by_courseware" +
                " WHERE deployment_id = ?" +
                " AND courseware_element_id = ?" +
                " AND student_id = ?" +
                " LIMIT ?";


        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, coursewareElementId, studentId, limit);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public BKTPathwayProgress fromRow(Row row) {
        BKTPathwayProgress pathwayProgress = new BKTPathwayProgress()
                .setId(row.getUUID("id"))
                .setAttemptId(row.getUUID("attempt_id"))
                .setChangeId(row.getUUID("change_id"))
                .setCompletedWalkables(row.getList("child_completed", UUID.class))
                .setChildWalkableCompletionConfidences(row.getMap("child_completion_confidences", UUID.class, Float.class))
                .setChildWalkableCompletionValues(row.getMap("child_completion_values", UUID.class, Float.class))
                .setCoursewareElementId(row.getUUID("courseware_element_id"))
                .setCoursewareElementType(Enums.of(CoursewareElementType.class, row.getString("courseware_element_type")))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setEvaluationId(row.getUUID("evaluation_id"))
                .setInProgressElementId(row.getUUID("in_progress_element_id"))
                .setInProgressElementType(getNullableEnum(row, "in_progress_element_type", CoursewareElementType.class))
                .setStudentId(row.getUUID("student_id"))
                .setpLnMinusGivenActual(row.getDouble("p_ln_minus_given_actual"))
                .setpLn(row.getDouble("p_ln"))
                .setpCorrect(row.getDouble("p_correct"));

        Completion completion = new Completion()
                .setValue(getNullableFloat(row, "completion_value"))
                .setConfidence(getNullableFloat(row, "completion_confidence"));

        if (completion.getValue() != null || completion.getConfidence() != null) {
            pathwayProgress.setCompletion(completion);
        }

        return pathwayProgress;
    }
}
