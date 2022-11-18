package com.smartsparrow.rtm.subscription.courseware.manualgrading;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ComponentManualGradingBroadcastMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ManualGradingConfig;

/**
 * This RTM producer produces an RTM event for a newly create activity component manual grading configuration
 */
public class ComponentConfigurationCreatedRTMProducer extends AbstractProducer<ComponentConfigurationCreatedRTMConsumable> {

    private ComponentConfigurationCreatedRTMConsumable componentConfigurationCreatedRTMConsumable;

    @Inject
    public ComponentConfigurationCreatedRTMProducer() {
    }

    public ComponentConfigurationCreatedRTMProducer buildComponentConfigurationCreatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                                                    UUID activityId,
                                                                                                    UUID componentId,
                                                                                                    ManualGradingConfig manualGradingConfig) {
        this.componentConfigurationCreatedRTMConsumable = new ComponentConfigurationCreatedRTMConsumable(
                rtmClientContext,
                new ComponentManualGradingBroadcastMessage(activityId, componentId, manualGradingConfig));
        return this;
    }

    @Override
    public ComponentConfigurationCreatedRTMConsumable getEventConsumable() {
        return componentConfigurationCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentConfigurationCreatedRTMProducer that = (ComponentConfigurationCreatedRTMProducer) o;
        return Objects.equals(componentConfigurationCreatedRTMConsumable,
                              that.componentConfigurationCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentConfigurationCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ComponentConfigurationCreatedRTMProducer{" +
                "componentConfigurationCreatedRTMConsumable=" + componentConfigurationCreatedRTMConsumable +
                '}';
    }
}
