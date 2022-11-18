package com.smartsparrow.learner.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.attempt.Attempt;

import reactor.core.publisher.Mono;

public class AttemptStubs {

    //
    //        ------------
    //       | activityId |
    //        ------------
    //              |
    //              |
    //        -------------
    //       |  pathwayId  |
    //        -------------
    //              |
    //              |
    //       ---------------
    //      | interactiveId |
    //       ---------------


    public static final UUID deploymentId = UUID.randomUUID();
    public static final UUID interactiveId = UUID.randomUUID();
    public static final UUID pathwayId = UUID.randomUUID();
    public static final UUID activityId = UUID.randomUUID();
    public static final UUID studentId = UUID.randomUUID();

    public static final UUID interactiveAttemptId = UUID.randomUUID();
    public static final UUID pathwayAttemptId = UUID.randomUUID();
    public static final UUID activityAttemptId = UUID.randomUUID();

    public static void mockAttemptService(AttemptService attemptService) {
        when(attemptService.newAttempt(eq(deploymentId), eq(studentId), any(CoursewareElementType.class), any(UUID.class), any()))
                .thenAnswer(invocation -> {
                    UUID elementId = invocation.getArgument(3);
                    UUID attemptId = elementId.equals(interactiveId) ? interactiveAttemptId : elementId.equals(pathwayId) ? pathwayAttemptId : activityAttemptId;
                    return Mono.just(new Attempt()
                            .setId(attemptId)
                            .setDeploymentId(invocation.getArgument(0))
                            .setStudentId(invocation.getArgument(1))
                            .setCoursewareElementType(invocation.getArgument(2))
                            .setCoursewareElementId(elementId)
                            .setParentId(invocation.getArgument(4))
                            .setValue(1));
                });

        when(attemptService.newAttempt(eq(deploymentId), eq(studentId), eq(CoursewareElementType.INTERACTIVE),
                eq(interactiveId), eq(pathwayAttemptId), anyInt()))
                .thenAnswer(invocation -> {
                    UUID elementId = invocation.getArgument(3);
                    UUID attemptId = elementId.equals(interactiveId) ? interactiveAttemptId : elementId.equals(pathwayId) ? pathwayAttemptId : activityAttemptId;
                    return Mono.just(new Attempt()
                            .setId(attemptId)
                            .setDeploymentId(invocation.getArgument(0))
                            .setStudentId(invocation.getArgument(1))
                            .setCoursewareElementType(invocation.getArgument(2))
                            .setCoursewareElementId(invocation.getArgument(3))
                            .setParentId(invocation.getArgument(4))
                            .setValue(invocation.getArgument(5)));
                });

    }
}
