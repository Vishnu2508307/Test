package com.smartsparrow.rtm.message.recv.learner.annotation;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.AnnotationMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class UpdateLearnerAnnotationMessage extends ReceivedMessage implements AnnotationMessage, DeploymentMessage {

    private UUID elementId;
    private UUID deploymentId;
    private CoursewareElementType elementType;
    private UUID annotationId;
    private Motivation motivation;
    private String body;
    private String target;

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    @Override
    public UUID getAnnotationId() {
        return annotationId;
    }

    public String getBody() {
        return body;
    }

    public String getTarget() {
        return target;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateLearnerAnnotationMessage that = (UpdateLearnerAnnotationMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                elementType == that.elementType &&
                Objects.equals(annotationId, that.annotationId) &&
                motivation == that.motivation &&
                Objects.equals(body, that.body) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, deploymentId, elementType, annotationId, motivation, body, target);
    }

    @Override
    public String toString() {
        return "UpdateLearnerAnnotationMessage{" +
                "elementId=" + elementId +
                ", deploymentId=" + deploymentId +
                ", elementType=" + elementType +
                ", annotationId=" + annotationId +
                ", motivation=" + motivation +
                ", body='" + body + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
