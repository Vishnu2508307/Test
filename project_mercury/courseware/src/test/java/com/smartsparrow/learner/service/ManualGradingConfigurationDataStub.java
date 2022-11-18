package com.smartsparrow.learner.service;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.LearnerManualGradingConfiguration;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.learner.data.ManualGradeEntry;
import com.smartsparrow.util.UUIDs;

public class ManualGradingConfigurationDataStub {

    public static LearnerManualGradingConfiguration buildManualGradingConfiguration(UUID deploymentId, UUID changeId) {
        return new LearnerManualGradingConfiguration()
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
                .setComponentId(UUID.randomUUID())
                .setMaxScore(10d)
                .setParentId(UUID.randomUUID())
                .setParentType(CoursewareElementType.INTERACTIVE);
    }

    public static ManualGradeEntry buildManualGradeEntry(final LearnerManualGradingConfiguration configuration,
                                                         final UUID studentId, final UUID attemptId) {
        return new ManualGradeEntry()
                .setId(UUIDs.timeBased())
                .setDeploymentId(configuration.getDeploymentId())
                .setStudentId(studentId)
                .setComponentId(configuration.getComponentId())
                .setAttemptId(attemptId)
                .setMaxScore(configuration.getMaxScore())
                .setScore(1d)
                .setChangeId(configuration.getChangeId())
                .setParentId(configuration.getParentId())
                .setParentType(configuration.getParentType())
                .setOperator(MutationOperator.SET)
                .setInstructorId(UUID.randomUUID());
    }
}
