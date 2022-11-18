package com.smartsparrow.learner.service;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.learner.data.ManualGradeEntry;
import com.smartsparrow.util.UUIDs;

public class ManualGradeEntryDataStub {

    public static ManualGradeEntry createManualGradeEntry(UUID deploymentId) {
        return new ManualGradeEntry()
                .setDeploymentId(deploymentId)
                .setStudentId(UUID.randomUUID())
                .setComponentId(UUID.randomUUID())
                .setAttemptId(UUID.randomUUID())
                .setId(UUIDs.timeBased())
                .setMaxScore(10d)
                .setScore(10d)
                .setChangeId(UUID.randomUUID())
                .setParentId(UUID.randomUUID())
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setOperator(MutationOperator.ADD)
                .setInstructorId(UUID.randomUUID());
    }
}
