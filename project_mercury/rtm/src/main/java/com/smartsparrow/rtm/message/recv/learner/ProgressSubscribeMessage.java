package com.smartsparrow.rtm.message.recv.learner;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ProgressSubscribeMessage extends ReceivedMessage {

    private UUID deploymentId;
    private UUID coursewareElementId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressSubscribeMessage that = (ProgressSubscribeMessage) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(coursewareElementId, that.coursewareElementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, coursewareElementId);
    }

    @Override
    public String toString() {
        return "ProgressSubscribeMessage{" +
                "deploymentId=" + deploymentId +
                ", coursewareElementId=" + coursewareElementId +
                "} " + super.toString();
    }
}
