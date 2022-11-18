package com.smartsparrow.rtm.subscription.courseware.moved;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a moved component
 */
public class ComponentMovedRTMProducer extends AbstractProducer<ComponentMovedRTMConsumable> {

    private ComponentMovedRTMConsumable componentMovedRTMConsumable;

    @Inject
    public ComponentMovedRTMProducer() {
    }

    public ComponentMovedRTMProducer buildComponentMovedRTMConsumable(RTMClientContext rtmClientContext,
                                                                      UUID activityId,
                                                                      UUID componentId) {
        this.componentMovedRTMConsumable = new ComponentMovedRTMConsumable(rtmClientContext,
                                                                             new ActivityBroadcastMessage(activityId,
                                                                                                          componentId,
                                                                                                          CoursewareElementType.COMPONENT));
        return this;
    }

    @Override
    public ComponentMovedRTMConsumable getEventConsumable() {
        return componentMovedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentMovedRTMProducer that = (ComponentMovedRTMProducer) o;
        return Objects.equals(componentMovedRTMConsumable, that.componentMovedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentMovedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ComponentMovedRTMProducer{" +
                "componentMovedRTMConsumable=" + componentMovedRTMConsumable +
                '}';
    }
}
