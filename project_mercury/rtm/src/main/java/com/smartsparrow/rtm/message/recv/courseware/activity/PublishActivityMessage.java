package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.cohort.CohortMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PublishActivityMessage extends ReceivedMessage implements CoursewareElementMessage, CohortMessage {

    private UUID activityId;
    private UUID deploymentId;
    private UUID cohortId;
    private boolean lockPluginVersionEnabled;

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    @Override
    public UUID getCohortId() {
        return cohortId;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return activityId;
    }

    public boolean isLockPluginVersionEnabled() {
        return lockPluginVersionEnabled;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.ACTIVITY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublishActivityMessage that = (PublishActivityMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(deploymentId, that.deploymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, deploymentId);
    }

    @Override
    public String toString() {
        return "PublishActivityMessage{" +
                "activityId=" + activityId +
                ", deploymentId=" + deploymentId +
                "} " + super.toString();
    }
}
