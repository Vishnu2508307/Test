package com.smartsparrow.rtm.subscription.courseware.created;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ComponentCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly create activity component
 */
public class ComponentCreatedRTMProducer extends AbstractProducer<ComponentCreatedRTMConsumable> {

    private ComponentCreatedRTMConsumable componentCreatedRTMConsumable;

    @Inject
    public ComponentCreatedRTMProducer() {
    }

    public ComponentCreatedRTMProducer buildComponentCreatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                          UUID activityId,
                                                                          UUID componentId) {
        this.componentCreatedRTMConsumable = new ComponentCreatedRTMConsumable(rtmClientContext,
                                                                               new ComponentCreatedBroadcastMessage(
                                                                                       activityId,
                                                                                       componentId));
        return this;
    }

    @Override
    public ComponentCreatedRTMConsumable getEventConsumable() {
        return componentCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentCreatedRTMProducer that = (ComponentCreatedRTMProducer) o;
        return Objects.equals(componentCreatedRTMConsumable, that.componentCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ComponentCreatedRTMProducer{" +
                "componentCreatedRTMConsumable=" + componentCreatedRTMConsumable +
                '}';
    }
}
