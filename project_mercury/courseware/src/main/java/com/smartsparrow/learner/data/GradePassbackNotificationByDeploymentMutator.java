package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class GradePassbackNotificationByDeploymentMutator extends SimpleTableMutator<GradePassbackNotification> {

    @Override
    public Statement upsert(GradePassbackNotification mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO learner.grade_passback_notification_by_deployment (" +
                " deployment_id" +
                ", change_id" +
                ", student_id" +
                ", courseware_element_id" +
                ", courseware_element_type" +
                ", notification_id" +
                ", result_score" +
                ", status" +
                ", completed_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, GradePassbackNotification mutation) {
        stmt.setUUID(0, mutation.getDeploymentId());
        stmt.setUUID(1, mutation.getChangeId());
        stmt.setUUID(2, mutation.getStudentId());
        stmt.setUUID(3, mutation.getCoursewareElementId());
        stmt.setString(4, Enums.asString(mutation.getCoursewareElementType()));
        stmt.setUUID(5, mutation.getNotificationId());
        stmt.setDouble(6, mutation.getResultScore());
        stmt.setString(7, Enums.asString(mutation.getStatus()));
        Mutators.bindNonNull(stmt, 8, mutation.getCompletedAt());
    }
}
