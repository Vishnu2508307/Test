package com.smartsparrow.learner.lang;

import java.util.UUID;

public class DeploymentNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "deployment not found for activity %s and deployment %s";

    private final UUID activityId;
    private final UUID deploymentId;

    public DeploymentNotFoundException(UUID activityId, UUID deploymentId) {
        super(String.format(ERROR_MESSAGE, activityId, deploymentId));
        this.activityId = activityId;
        this.deploymentId = deploymentId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }
}
