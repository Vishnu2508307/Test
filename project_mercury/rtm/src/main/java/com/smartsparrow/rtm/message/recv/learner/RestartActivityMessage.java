package com.smartsparrow.rtm.message.recv.learner;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class RestartActivityMessage extends ReceivedMessage {

    private UUID activityId;
    private UUID deploymentId;

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestartActivityMessage that = (RestartActivityMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(deploymentId, that.deploymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, deploymentId);
    }
}
