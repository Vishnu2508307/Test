package com.smartsparrow.rtm.message.recv.learner.annotation;

import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.CoursewareElementMotivationMessage;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateLearnerAnnotationMessage extends ReceivedMessage implements CoursewareElementMotivationMessage, DeploymentMessage {

    private UUID deploymentId;
    private UUID elementId;
    private CoursewareElementType elementType;
    private Motivation motivation;
    private String body;
    private String target;

    @Override
    public Motivation getMotivation() {
        return motivation;
    }

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }
    
    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public String getBody() {
        return body;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateLearnerAnnotationMessage that = (CreateLearnerAnnotationMessage) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                motivation == that.motivation &&
                Objects.equals(body, that.body) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, elementId, elementType, motivation, body, target);
    }

    @Override
    public String toString() {
        return "CreateLearnerAnnotationMessage{" +
                "deploymentId=" + deploymentId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", motivation=" + motivation +
                ", body='" + body + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
