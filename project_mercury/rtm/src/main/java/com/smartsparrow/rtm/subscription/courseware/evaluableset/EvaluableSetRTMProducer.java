package com.smartsparrow.rtm.subscription.courseware.evaluableset;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.EvaluableSetBroadcastMessage;

/**
 * This RTM producer produces an RTM event for evaluable set
 */
public class EvaluableSetRTMProducer extends AbstractProducer<EvaluableSetRTMConsumable> {

    private EvaluableSetRTMConsumable evaluableSetRTMConsumable;

    @Inject
    public EvaluableSetRTMProducer() {
    }

    public EvaluableSetRTMProducer buildEvaluableSetRTMConsumable(RTMClientContext rtmClientContext,
                                                                  UUID activityId,
                                                                  UUID elementId,
                                                                  CoursewareElementType elementType,
                                                                  EvaluationMode evaluationMode) {
        this.evaluableSetRTMConsumable = new EvaluableSetRTMConsumable(rtmClientContext,
                                                                       new EvaluableSetBroadcastMessage(
                                                                               activityId,
                                                                               elementId,
                                                                               elementType,
                                                                               evaluationMode));
        return this;
    }

    @Override
    public EvaluableSetRTMConsumable getEventConsumable() {
        return evaluableSetRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluableSetRTMProducer that = (EvaluableSetRTMProducer) o;
        return Objects.equals(evaluableSetRTMConsumable, that.evaluableSetRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluableSetRTMConsumable);
    }

    @Override
    public String toString() {
        return "EvaluableSetRTMProducer{" +
                "evaluableSetRTMConsumable=" + evaluableSetRTMConsumable +
                '}';
    }
}
