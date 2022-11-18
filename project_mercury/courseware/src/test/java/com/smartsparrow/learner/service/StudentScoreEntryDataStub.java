package com.smartsparrow.learner.service;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.StudentScoreEntry;

public class StudentScoreEntryDataStub {

    public static StudentScoreEntry studentScoreEntry(Double adjustmentValue) {
        return new StudentScoreEntry()
                .setAdjustmentValue(adjustmentValue);
    }

    public static StudentScoreEntry buildStudentScoreEntry(final Double value, final MutationOperator operator,
                                                           final Deployment deployment, final UUID studentId) {
        return new StudentScoreEntry()
                .setId(UUID.randomUUID())
                .setValue(value)
                .setAdjustmentValue(value)
                .setSourceAccountId(null)
                .setSourceScenarioId(UUID.randomUUID())
                .setSourceElementId(null)
                .setElementType(CoursewareElementType.INTERACTIVE)
                .setElementId(UUID.randomUUID())
                .setEvaluationId(UUID.randomUUID())
                .setCohortId(deployment.getCohortId())
                .setDeploymentId(deployment.getId())
                .setChangeId(deployment.getChangeId())
                .setAttemptId(UUID.randomUUID())
                .setOperator(operator)
                .setStudentId(studentId);
    }
}
