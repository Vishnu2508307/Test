package com.smartsparrow.learner.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.DeploymentLogGateway;
import com.smartsparrow.learner.data.DeploymentStepLog;
import com.smartsparrow.learner.data.DeploymentStepState;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

@Singleton
public class DeploymentLogService {

    private final DeploymentLogGateway deploymentLogGateway;

    @Inject
    public DeploymentLogService(DeploymentLogGateway deploymentLogGateway) {
        this.deploymentLogGateway = deploymentLogGateway;
    }

    public Mono<DeploymentStepLog> logStartedStep(final Deployment deployment, final UUID elementId,
                                                  final CoursewareElementType elementType, final String message) {
        return logStep(deployment, new CoursewareElement()
                .setElementId(elementId)
                .setElementType(elementType), DeploymentStepState.STARTED, message);
    }

    public Mono<DeploymentStepLog> logProgressStep(final Deployment deployment, final UUID elementId,
                                                   final CoursewareElementType elementType, final String message) {
        return logStep(deployment, new CoursewareElement()
                .setElementId(elementId)
                .setElementType(elementType), DeploymentStepState.IN_PROGRESS, message);
    }

    public Mono<DeploymentStepLog> logCompletedStep(final Deployment deployment, final UUID elementId,
                                                    final CoursewareElementType elementType, final String message) {
        return logStep(deployment, new CoursewareElement()
                .setElementId(elementId)
                .setElementType(elementType), DeploymentStepState.COMPLETED, message);
    }

    public Mono<DeploymentStepLog> logFailedStep(final Deployment deployment, final UUID elementId,
                                                 final CoursewareElementType elementType, final String message) {
        return logStep(deployment, new CoursewareElement()
                .setElementId(elementId)
                .setElementType(elementType), DeploymentStepState.FAILED, message);
    }

    /**
     * Create and persist a deployment log step
     *
     * @param deployment the deployment to create the log step for
     * @param coursewareElement the courseware element the log refers to
     * @param state the state of the step
     * @param message the message
     * @return a mono of deployment step log
     */
    private Mono<DeploymentStepLog> logStep(final Deployment deployment, final CoursewareElement coursewareElement,
                                            final DeploymentStepState state, final String message) {
        DeploymentStepLog deploymentStepLog = new DeploymentStepLog()
                .setDeployment(deployment)
                .setElement(coursewareElement)
                .setState(state)
                .setMessage(message)
                .setId(UUIDs.timeBased());

        return deploymentLogGateway.persist(deploymentStepLog)
                .singleOrEmpty()
                .thenReturn(deploymentStepLog);
    }
}
