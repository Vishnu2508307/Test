package com.smartsparrow.learner.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.DeploymentStepLog;

import reactor.core.publisher.Mono;

public class DeploymentLogServiceStub {

    /**
     * Mock all the log methods for the deployment log service
     *
     * @param deploymentLogService the deployment log service to mock all the methods for
     */
    public static void mockLogMethods(final DeploymentLogService deploymentLogService) {
        when(deploymentLogService.logStartedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString()))
                .thenReturn(Mono.just(new DeploymentStepLog()));
        when(deploymentLogService.logProgressStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString()))
                .thenReturn(Mono.just(new DeploymentStepLog()));
        when(deploymentLogService.logCompletedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString()))
                .thenReturn(Mono.just(new DeploymentStepLog()));
        when(deploymentLogService.logFailedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString()))
                .thenReturn(Mono.just(new DeploymentStepLog()));
    }
}
