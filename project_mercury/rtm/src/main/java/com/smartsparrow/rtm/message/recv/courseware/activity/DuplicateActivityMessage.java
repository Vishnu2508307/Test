package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;
import com.smartsparrow.rtm.message.recv.courseware.pathway.PathwayMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DuplicateActivityMessage extends ReceivedMessage implements CoursewareElementMessage, PathwayMessage {

    private UUID activityId;
    private UUID parentPathwayId;
    private Integer index;
    private boolean newDuplicateFlow;   // feature flag control by launchDarkly

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getParentPathwayId() {
        return parentPathwayId;
    }

    public Integer getIndex() {
        return index;
    }

    public Boolean getNewDuplicateFlow() {
        return newDuplicateFlow;
    }

    public DuplicateActivityMessage setNewDuplicateFlow(Boolean newDuplicateFlow) {
        this.newDuplicateFlow = newDuplicateFlow;
        return this;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return activityId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.ACTIVITY;
    }

    @JsonIgnore
    @Override
    public UUID getPathwayId() {
        return parentPathwayId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DuplicateActivityMessage that = (DuplicateActivityMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(parentPathwayId, that.parentPathwayId) &&
                Objects.equals(newDuplicateFlow, that.newDuplicateFlow)   &&
                Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, parentPathwayId, index, newDuplicateFlow);
    }

    @Override
    public String toString() {
        return "DuplicateActivityMessage{" +
                "activityId=" + activityId +
                ", parentPathwayId=" + parentPathwayId +
                ", index=" + index +
                ", newDuplicateFlow=" + newDuplicateFlow +
                "} " + super.toString();
    }
}
