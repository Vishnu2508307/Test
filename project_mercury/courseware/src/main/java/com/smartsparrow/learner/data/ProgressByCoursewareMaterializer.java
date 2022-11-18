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
import com.smartsparrow.learner.progress.GeneralProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.util.Enums;

class ProgressByCoursewareMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    // @formatter:off
    private static final String QUERY = "SELECT "
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
            + " FROM learner.progress_by_courseware"
            + " WHERE deployment_id = ?"
            + "   AND courseware_element_id = ?"
            + "   AND student_id = ?";
    // @formatter:on

    @Inject
    ProgressByCoursewareMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        //
        return query(QUERY, deploymentId, coursewareElementId, studentId);
    }

    public Statement findLatest(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        //
        String LIMITED = QUERY + " LIMIT 1";
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
    public Progress fromRow(final Row row) {
        GeneralProgress progress = new GeneralProgress() //
                .setId(row.getUUID("id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setCoursewareElementId(row.getUUID("courseware_element_id"))
                .setCoursewareElementType(Enums.of(CoursewareElementType.class, //
                                                   row.getString("courseware_element_type")))
                .setStudentId(row.getUUID("student_id"))
                .setAttemptId(row.getUUID("attempt_id"))
                .setEvaluationId(row.getUUID("evaluation_id"));

        Completion completion = new Completion()
                .setValue(getNullableFloat(row, "completion_value"))
                .setConfidence(getNullableFloat(row, "completion_confidence"));

        if (completion.getValue() != null || completion.getConfidence() != null) {
            progress.setCompletion(completion);
        }

        return progress;
    }
}
