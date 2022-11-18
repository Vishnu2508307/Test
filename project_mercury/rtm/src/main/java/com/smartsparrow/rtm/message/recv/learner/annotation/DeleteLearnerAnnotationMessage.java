package com.smartsparrow.rtm.message.recv.learner.annotation;

import com.smartsparrow.rtm.message.ReceivedMessage;

import com.smartsparrow.rtm.message.recv.learner.LearnerAnnotationMessage;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteLearnerAnnotationMessage extends ReceivedMessage implements LearnerAnnotationMessage {

    private UUID annotationId;

    @Override
    public UUID getAnnotationId() {
        return annotationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteLearnerAnnotationMessage that = (DeleteLearnerAnnotationMessage) o;
        return Objects.equals(annotationId, that.annotationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationId);
    }

    @Override
    public String toString() {
        return "DeleteLearnerAnnotationMessage{" +
                "annotationId=" + annotationId +
                '}';
    }
}
