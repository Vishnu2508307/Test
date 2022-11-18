package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.recv.courseware.activity.CreateActivityMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateProjectActivityMessage extends CreateActivityMessage implements ProjectMessage {

    private UUID projectId;
    private UUID activityId;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CreateProjectActivityMessage that = (CreateProjectActivityMessage) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), projectId, activityId);
    }

    @Override
    public String toString() {
        return "CreateProjectActivityMessage{" +
                "projectId=" + projectId +
                ", activityId=" + activityId +
                "} " + super.toString();
    }
}
