package com.smartsparrow.rtm.subscription.courseware.manualgrading;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a component manual grading config deleted event
 */
public class ComponentManualGradingConfigDeletedRTMProducer extends AbstractProducer<ComponentManualGradingConfigDeletedRTMConsumable> {

    private ComponentManualGradingConfigDeletedRTMConsumable componentManualGradingConfigDeletedRTMConsumable;

    @Inject
    public ComponentManualGradingConfigDeletedRTMProducer() {
    }

    public ComponentManualGradingConfigDeletedRTMProducer buildManualGradingConfigDeletedRTMConsumable(RTMClientContext rtmClientContext,
                                                                                                       UUID activityId,
                                                                                                       UUID componentId) {
        this.componentManualGradingConfigDeletedRTMConsumable = new ComponentManualGradingConfigDeletedRTMConsumable(
                rtmClientContext,
                new ActivityBroadcastMessage(activityId, componentId, COMPONENT));
        return this;
    }

    @Override
    public ComponentManualGradingConfigDeletedRTMConsumable getEventConsumable() {
        return componentManualGradingConfigDeletedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentManualGradingConfigDeletedRTMProducer that = (ComponentManualGradingConfigDeletedRTMProducer) o;
        return Objects.equals(componentManualGradingConfigDeletedRTMConsumable, that.componentManualGradingConfigDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentManualGradingConfigDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ComponentManualGradingConfigDeletedRTMProducer{" +
                "componentManualGradingConfigDeletedRTMConsumable=" + componentManualGradingConfigDeletedRTMConsumable +
                '}';
    }
}
