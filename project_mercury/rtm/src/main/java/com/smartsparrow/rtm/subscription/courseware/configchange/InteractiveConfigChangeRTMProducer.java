package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a config changed interactive
 */
public class InteractiveConfigChangeRTMProducer extends AbstractProducer<InteractiveConfigChangeRTMConsumable> {

    private InteractiveConfigChangeRTMConsumable interactiveConfigChangeRTMConsumable;

    @Inject
    public InteractiveConfigChangeRTMProducer() {
    }

    public InteractiveConfigChangeRTMProducer buildInteractiveConfigChangeRTMConsumable(RTMClientContext rtmClientContext,
                                                                                        UUID activityId,
                                                                                        UUID elementId,
                                                                                        String config) {
        this.interactiveConfigChangeRTMConsumable = new InteractiveConfigChangeRTMConsumable(rtmClientContext,
                                                                                             new ConfigChangeBroadcastMessage(
                                                                                                     activityId,
                                                                                                     elementId,
                                                                                                     INTERACTIVE,
                                                                                                     config));
        return this;
    }

    @Override
    public InteractiveConfigChangeRTMConsumable getEventConsumable() {
        return interactiveConfigChangeRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveConfigChangeRTMProducer that = (InteractiveConfigChangeRTMProducer) o;
        return Objects.equals(interactiveConfigChangeRTMConsumable, that.interactiveConfigChangeRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveConfigChangeRTMConsumable);
    }

    @Override
    public String toString() {
        return "InteractiveConfigChangeRTMProducer{" +
                "interactiveConfigChangeRTMConsumable=" + interactiveConfigChangeRTMConsumable +
                '}';
    }
}
