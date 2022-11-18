package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;

public class EvaluableSetBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -7725218759104347511L;

    private final EvaluationMode evaluationMode;

    public EvaluableSetBroadcastMessage(final UUID activityId,
                                        final UUID elementId,
                                        final CoursewareElementType elementType,
                                        final EvaluationMode evaluationMode) {
        super(activityId, elementId, elementType);
        this.evaluationMode = evaluationMode;
    }

    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EvaluableSetBroadcastMessage that = (EvaluableSetBroadcastMessage) o;
        return evaluationMode == that.evaluationMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), evaluationMode);
    }

    @Override
    public String toString() {
        return "EvaluableSetBroadcastMessage{" +
                "evaluationMode=" + evaluationMode +
                "} " + super.toString();
    }
}
