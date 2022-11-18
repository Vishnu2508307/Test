package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;
import com.smartsparrow.rtm.message.recv.workspace.ProjectMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DuplicateActivityProjectMessage extends ReceivedMessage implements ProjectMessage, CoursewareElementMessage {

    private UUID activityId;
    private UUID projectId;
    private boolean newDuplicateFlow;   // feature flag control by launchDarkly

    @Override
    public UUID getElementId() {
        return activityId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.ACTIVITY;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public Boolean getNewDuplicateFlow() {
        return newDuplicateFlow;
    }

    public DuplicateActivityProjectMessage setNewDuplicateFlow(Boolean newDuplicateFlow) {
        this.newDuplicateFlow = newDuplicateFlow;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DuplicateActivityProjectMessage that = (DuplicateActivityProjectMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(newDuplicateFlow, that.newDuplicateFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, projectId, newDuplicateFlow);
    }

    @Override
    public String toString() {
        return "DuplicateActivityProjectMessage{" +
                "activityId=" + activityId +
                ", projectId=" + projectId +
                ", newDuplicateFlow=" + newDuplicateFlow +
                "} " + super.toString();
    }
}
