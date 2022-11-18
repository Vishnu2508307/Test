package com.smartsparrow.rtm.message.recv.learner;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.learner.annotation.CreatorMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListLearnerAnnotationMessage extends ReceivedMessage implements CreatorMessage {

    UUID deploymentId;
    UUID creatorAccountId;
    UUID elementId;
    Motivation motivation;
    CoursewareElementType elementType;

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    @Override
    public UUID getCreatorAccountId() {
        return creatorAccountId;
    }

    public UUID getElementId() {
        return elementId;
    }

    public Motivation getMotivation() {
        return motivation;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListLearnerAnnotationMessage that = (ListLearnerAnnotationMessage) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(creatorAccountId, that.creatorAccountId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                motivation == that.motivation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, creatorAccountId, elementId, elementType, motivation);
    }

    @Override
    public String toString() {
        return "ListLearnerAnnotationMessage{" +
                "deploymentId=" + deploymentId +
                ", creatorAccountId=" + creatorAccountId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", motivation=" + motivation +
                '}';
    }

}
