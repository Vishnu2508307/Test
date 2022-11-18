package com.smartsparrow.rtm.message.recv.courseware;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CoursewareEvaluableMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private EvaluationMode evaluationMode;

    @Override
    public UUID getElementId() { return elementId; }

    @Override
    public CoursewareElementType getElementType() { return elementType; }

    public EvaluationMode getEvaluationMode() { return evaluationMode; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareEvaluableMessage that = (CoursewareEvaluableMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                evaluationMode == that.evaluationMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, evaluationMode);
    }

    @Override
    public String toString() {
        return "CoursewareEvaluableMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", evaluationMode=" + evaluationMode +
                '}';
    }
}
