package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

import javax.inject.Inject;
import java.util.UUID;

class GradePassbackNotificationByDeploymentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public GradePassbackNotificationByDeploymentMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID notificationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                " deployment_id" +
                ", change_id" +
                ", student_id" +
                ", courseware_element_id" +
                ", courseware_element_type" +
                ", notification_id" +
                ", result_score" +
                ", status" +
                ", completed_at" +
                "  FROM learner.grade_passback_notification_by_deployment" +
                "  WHERE deployment_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(notificationId);
        return stmt;
    }

    public GradePassbackNotification fromRow(Row row) {
        return new GradePassbackNotification()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setStudentId(row.getUUID("student_id"))
                .setCoursewareElementId(row.getUUID("courseware_element_id"))
                .setCoursewareElementType(Enums.of(CoursewareElementType.class, row.getString("courseware_element_type")))
                .setNotificationId(row.getUUID("notification_id"))
                .setResultScore(row.getDouble("result_score"))
                .setStatus(Enums.of(GradePassbackNotification.Status.class, row.getString("status")))
                .setCompletedAt(row.getUUID("completed_at"));
    }

}
