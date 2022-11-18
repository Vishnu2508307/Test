package com.smartsparrow.learner.data;

import static com.smartsparrow.dse.api.Mutators.bindNonNull;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ManualGradeEntryMutator extends SimpleTableMutator<ManualGradeEntry> {

    @Override
    public String getUpsertQuery(ManualGradeEntry mutation) {
        return "INSERT INTO learner.manual_grade_entry_by_student (" +
                " deployment_id" +
                ", student_id" +
                ", component_id" +
                ", attempt_id" +
                ", id" +
                ", max_score" +
                ", score" +
                ", change_id" +
                ", parent_id" +
                ", parent_type" +
                ", operator" +
                ", instructor_id" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ManualGradeEntry mutation) {
        stmt.setUUID(0, mutation.getDeploymentId());
        stmt.setUUID(1, mutation.getStudentId());
        stmt.setUUID(2, mutation.getComponentId());
        stmt.setUUID(3, mutation.getAttemptId());
        stmt.setUUID(4, mutation.getId());
        bindNonNull(stmt, 5, mutation.getMaxScore(), Double.class);
        stmt.setDouble(6, mutation.getScore());
        stmt.setUUID(7, mutation.getChangeId());
        stmt.setUUID(8, mutation.getParentId());
        stmt.setString(9, Enums.asString(mutation.getParentType()));
        stmt.setString(10, Enums.asString(mutation.getOperator()));
        stmt.setUUID(11, mutation.getInstructorId());
    }
}
