package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a config changed component
 */
public class ComponentConfigChangeRTMProducer extends AbstractProducer<ComponentConfigChangeRTMConsumable> {

    private ComponentConfigChangeRTMConsumable componentConfigChangeRTMConsumable;

    @Inject
    public ComponentConfigChangeRTMProducer() {
    }

    public ComponentConfigChangeRTMProducer buildComponentConfigChangeRTMConsumable(RTMClientContext rtmClientContext,
                                                                                    UUID activityId,
                                                                                    UUID componentId,
                                                                                    String config) {
        this.componentConfigChangeRTMConsumable = new ComponentConfigChangeRTMConsumable(rtmClientContext,
                                                                                         new ConfigChangeBroadcastMessage(
                                                                                                 activityId,
                                                                                                 componentId,
                                                                                                 COMPONENT,
                                                                                                 config));
        return this;
    }

    @Override
    public ComponentConfigChangeRTMConsumable getEventConsumable() {
        return componentConfigChangeRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentConfigChangeRTMProducer that = (ComponentConfigChangeRTMProducer) o;
        return Objects.equals(componentConfigChangeRTMConsumable, that.componentConfigChangeRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentConfigChangeRTMConsumable);
    }

    @Override
    public String toString() {
        return "ComponentConfigChangeRTMProducer{" +
                "componentConfigChangeRTMConsumable=" + componentConfigChangeRTMConsumable +
                '}';
    }
}
